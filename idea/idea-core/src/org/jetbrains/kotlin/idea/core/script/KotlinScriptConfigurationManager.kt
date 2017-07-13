/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.core.script

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.search.NonClasspathDirectoriesScope
import com.intellij.util.io.URLUtil
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.future.future
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.script.*
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.script.dependencies.ScriptDependencies
import kotlin.script.dependencies.experimental.AsyncDependenciesResolver


// NOTE: this service exists exclusively because KotlinScriptConfigurationManager
// cannot be registered as implementing two services (state would be duplicated)
class IdeScriptExternalImportsProvider(
        private val scriptConfigurationManager: KotlinScriptConfigurationManager
) : KotlinScriptExternalImportsProvider {
    override fun getScriptDependencies(file: VirtualFile): ScriptDependencies? {
        return scriptConfigurationManager.getScriptDependencies(file)
    }
}

class DependenciesCache(private val project: Project) {
    private val cacheLock = ReentrantReadWriteLock()
    private val cache = hashMapOf<String, ScriptDependencies>()

    operator fun get(virtualFile: VirtualFile): ScriptDependencies? = cacheLock.read { cache[virtualFile.path] }

    private val allScriptsClasspathCache = ClearableLazyValue(cacheLock) {
        val files = cache.values.flatMap { it.classpath }.distinct()
        KotlinScriptConfigurationManager.toVfsRoots(files)
    }

    private val allScriptsClasspathScope = ClearableLazyValue(cacheLock) {
        NonClasspathDirectoriesScope(getAllScriptsClasspath())
    }

    private val allLibrarySourcesCache = ClearableLazyValue(cacheLock) {
        KotlinScriptConfigurationManager.toVfsRoots(cache.values.flatMap { it.sources }.distinct())
    }

    private val allLibrarySourcesScope = ClearableLazyValue(cacheLock) {
        NonClasspathDirectoriesScope(getAllLibrarySources())
    }

    fun getAllScriptsClasspath(): List<VirtualFile> = allScriptsClasspathCache.get()

    fun getAllLibrarySources(): List<VirtualFile> = allLibrarySourcesCache.get()

    fun getAllScriptsClasspathScope() = allScriptsClasspathScope.get()

    fun getAllLibrarySourcesScope() = allLibrarySourcesScope.get()

    fun onChange() {
        allScriptsClasspathCache.clear()
        allScriptsClasspathScope.clear()
        allLibrarySourcesCache.clear()
        allLibrarySourcesScope.clear()

        val kotlinScriptDependenciesClassFinder =
                Extensions.getArea(project).getExtensionPoint(PsiElementFinder.EP_NAME).extensions
                        .filterIsInstance<KotlinScriptDependenciesClassFinder>()
                        .single()

        kotlinScriptDependenciesClassFinder.clearCache()
    }

    fun clear() {
        cacheLock.write(cache::clear)
        onChange()
    }

    fun save(new: ScriptDependencies, virtualFile: VirtualFile): Boolean {
        val path = virtualFile.path
        val old = cacheLock.write {
            val old = cache[path]
            cache[path] = new
            old
        }
        return old == null || !new.match(old)
    }

    fun delete(virtualFile: VirtualFile): Boolean = cacheLock.write {
        cache.remove(virtualFile.path) != null
    }

}

class KotlinScriptConfigurationManager(
        private val project: Project,
        private val scriptDefinitionProvider: KotlinScriptDefinitionProvider
) : KotlinScriptExternalImportsProviderBase(project) {
    private val scriptDependencyUpdatesDispatcher = newFixedThreadPool(1).asCoroutineDispatcher()
    private val cache = DependenciesCache(project)
    private val requests = ConcurrentHashMap<String, DataAndRequest>()

    init {
        reloadScriptDefinitions()
        listenToVfsChanges()
    }

    private fun listenToVfsChanges() {
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener.Adapter() {

            val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
            val application = ApplicationManager.getApplication()

            override fun after(events: List<VFileEvent>) {
                if (updateCache(events.mapNotNull {
                    // The check is partly taken from the BuildManager.java
                    it.file?.takeIf {
                        // the isUnitTestMode check fixes ScriptConfigurationHighlighting & Navigation tests, since they are not trigger proper update mechanims
                        // TODO: find out the reason, then consider to fix tests and remove this check
                        (application.isUnitTestMode || projectFileIndex.isInContent(it)) && !ProjectUtil.isProjectOrWorkspaceFile(it)
                    }
                })) {
                    onChange()
                }
            }
        })
    }

    fun getScriptClasspath(file: VirtualFile): List<VirtualFile> = KotlinScriptConfigurationManager.toVfsRoots(getScriptDependencies(file).classpath)

    private fun notifyRootsChanged() {
        val rootsChangesRunnable = {
            runWriteAction {
                if (project.isDisposed) return@runWriteAction

                ProjectRootManagerEx.getInstanceEx(project)?.makeRootsChange(EmptyRunnable.getInstance(), false, true)
                ScriptDependenciesModificationTracker.getInstance(project).incModificationCount()
            }
        }

        val application = ApplicationManager.getApplication()
        if (application.isUnitTestMode) {
            rootsChangesRunnable.invoke()
        }
        else {
            application.invokeLater(rootsChangesRunnable, ModalityState.defaultModalityState())
        }
    }

    private fun reloadScriptDefinitions() {
        val def = makeScriptDefsFromTemplatesProviderExtensions(project, { ep, ex -> log.warn("[kts] Error loading definition from ${ep.id}", ex) })
        scriptDefinitionProvider.setScriptDefinitions(def)
    }

    private class TimeStampedRequest(val future: CompletableFuture<*>, val timeStamp: TimeStamp)

    private class DataAndRequest(
            val modificationStamp: Long?,
            val requestInProgress: TimeStampedRequest? = null
    )

    override fun getScriptDependencies(file: VirtualFile): ScriptDependencies {
        val cached = cache[file]
        cached?.let { return it }

        tryLoadingFromDisk(file)

        updateCache(listOf(file))

        return cache[file] ?: ScriptDependencies.Empty
    }

    private fun tryLoadingFromDisk(file: VirtualFile) {
        ScriptDependenciesFileAttribute.read(file)?.let { deserialized ->
            cache.save(deserialized, file)
            onChange()
        }
    }

    private fun updateCache(files: Iterable<VirtualFile>) =
        files.mapNotNull { file ->
            if (!file.isValid) {
                return cache.delete(file)
            }
            else {
                updateForFile(file)
            }
        }.contains(true)

    private fun updateForFile(file: VirtualFile): Boolean {
        val scriptDef = scriptDefinitionProvider.findScriptDefinition(file) ?: return false

        return when (scriptDef.dependencyResolver) {
            is AsyncDependenciesResolver -> updateAsync(file, scriptDef)
            else -> updateSync(file, scriptDef)
        }
    }

    private fun updateAsync(
            file: VirtualFile,
            scriptDefinition: KotlinScriptDefinition
    ): Boolean {
        val path = file.path
        val current = requests[path]

        if (!shouldSendNewRequest(file, current)) {
            return false
        }

        current?.requestInProgress?.future?.cancel(true)

        val (currentTimeStamp, newFuture) = sendRequest(path, scriptDefinition, file)

        requests[path] = DataAndRequest(
                file.modificationStamp,
                TimeStampedRequest(newFuture, currentTimeStamp)
        )
        return false // not changed immediately
    }

    private fun sendRequest(
            path: String,
            scriptDef: KotlinScriptDefinition,
            file: VirtualFile
    ): Pair<TimeStamp, CompletableFuture<*>> {
        val currentTimeStamp = TimeStamps.next()
        val dependenciesResolver = scriptDef.dependencyResolver as AsyncDependenciesResolver

        val newFuture = future(scriptDependencyUpdatesDispatcher) {
            dependenciesResolver.resolveAsync(
                    getScriptContents(scriptDef, file),
                    (scriptDef as? KotlinScriptDefinitionFromAnnotatedTemplate)?.environment.orEmpty()
            )
        }.thenAccept { result ->
            val lastTimeStamp = requests[path]?.requestInProgress?.timeStamp
            if (lastTimeStamp == currentTimeStamp) {
                ServiceManager.getService(project, ScriptReportSink::class.java)?.attachReports(file, result.reports)
                if (cache(result.dependencies ?: ScriptDependencies.Empty, file)) {
                    onChange()
                }
            }
        }
        return Pair(currentTimeStamp, newFuture)
    }

    private fun onChange() {
        cache.onChange()
        notifyRootsChanged()
    }

    private fun shouldSendNewRequest(file: VirtualFile, oldDataAndRequest: DataAndRequest?): Boolean {
        val currentStamp = file.modificationStamp
        val previousStamp = oldDataAndRequest?.modificationStamp

        if (currentStamp != previousStamp) {
            return true
        }

        return oldDataAndRequest.requestInProgress == null
    }

    private fun updateSync(file: VirtualFile, scriptDef: KotlinScriptDefinition): Boolean {
        val newDeps = resolveDependencies(scriptDef, file) ?: ScriptDependencies.Empty
        return cache(newDeps, file)
    }

    private fun cache(
            new: ScriptDependencies,
            file: VirtualFile
    ): Boolean {
        val updated = cache.save(new, file)
        if (updated) {
            ScriptDependenciesFileAttribute.write(file, new)
        }
        return updated
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): KotlinScriptConfigurationManager =
                ServiceManager.getService(project, KotlinScriptConfigurationManager::class.java)

        fun toVfsRoots(roots: Iterable<File>): List<VirtualFile> {
            return roots.mapNotNull { it.classpathEntryToVfs() }
        }

        private fun File.classpathEntryToVfs(): VirtualFile? {
            val res = when {
                !exists() -> null
                isDirectory -> StandardFileSystems.local()?.findFileByPath(this.canonicalPath)
                isFile -> StandardFileSystems.jar()?.findFileByPath(this.canonicalPath + URLUtil.JAR_SEPARATOR)
                else -> null
            }
            // TODO: report this somewhere, but do not throw: assert(res != null, { "Invalid classpath entry '$this': exists: ${exists()}, is directory: $isDirectory, is file: $isFile" })
            return res
        }

        internal val log = Logger.getInstance(KotlinScriptConfigurationManager::class.java)

        @TestOnly
        fun updateScriptDependenciesSynchronously(virtualFile: VirtualFile, project: Project) {
            with(getInstance(project)) {
                val scriptDefinition = KotlinScriptDefinitionProvider.getInstance(project)!!.findScriptDefinition(virtualFile)!!
                val updated = updateSync(virtualFile, scriptDefinition)
                assert(updated)
                cache.onChange()
                notifyRootsChanged()
            }
        }

        @TestOnly
        fun reloadScriptDefinitions(project: Project) {
            with(getInstance(project)) {
                reloadScriptDefinitions()
                cache.clear()
            }
        }
    }
}

private class ClearableLazyValue<out T : Any>(private val lock: ReentrantReadWriteLock, private val compute: () -> T) {
    private var value: T? = null

    fun get(): T {
        lock.read {
            if (value == null) {
                lock.write {
                    value = compute()
                }
            }
            return value!!
        }
    }

    fun clear() {
        lock.write {
            value = null
        }
    }
}

// TODO: relying on this to compare dependencies seems wrong, doesn't take javaHome and other stuff into account
private fun ScriptDependencies.match(other: ScriptDependencies)
        = classpath.isSamePathListAs(other.classpath) &&
          sources.toSet().isSamePathListAs(other.sources.toSet()) // TODO: gradle returns stdlib and reflect sources in unstable order for some reason


private fun Iterable<File>.isSamePathListAs(other: Iterable<File>): Boolean =
        with(Pair(iterator(), other.iterator())) {
            while (first.hasNext() && second.hasNext()) {
                if (first.next().canonicalPath != second.next().canonicalPath) return false
            }
            !(first.hasNext() || second.hasNext())
        }

data class TimeStamp(private val stamp: Long) {
    operator fun compareTo(other: TimeStamp) = this.stamp.compareTo(other.stamp)
}

object TimeStamps {
    private var current: Long = 0

    fun next() = TimeStamp(current++)
}