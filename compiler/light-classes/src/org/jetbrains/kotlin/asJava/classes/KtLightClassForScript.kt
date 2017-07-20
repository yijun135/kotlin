/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.asJava.classes

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.psi.*
import com.intellij.psi.impl.light.LightEmptyImplementsList
import com.intellij.psi.impl.light.LightModifierList
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.SLRUCache
import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.asJava.builder.LightClassDataHolder
import org.jetbrains.kotlin.asJava.builder.LightClassDataProviderForScript
import org.jetbrains.kotlin.asJava.elements.FakeFileForLightClass
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.load.java.structure.LightClassOriginKind
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.siblings
import javax.swing.Icon

class KtLightClassForScript private constructor(
        manager: PsiManager,
        private val scriptClassFqName: FqName,
        private val lightClassDataCache: CachedValue<LightClassDataHolder.ForScript>,
        files: Collection<KtFile>
) : KtLazyLightClass(manager) {
    private data class StubCacheKey(val fqName: FqName, val searchScope: GlobalSearchScope)

    class ScriptStubCache(private val project: Project) {
        private inner class ScriptCacheData {
            val cache = object : SLRUCache<StubCacheKey, CachedValue<LightClassDataHolder.ForScript>>(20, 30) {
                override fun createValue(key: StubCacheKey): CachedValue<LightClassDataHolder.ForScript> {
                    val stubProvider = LightClassDataProviderForScript.ByProjectSource(project, key.fqName, key.searchScope)
                    return CachedValuesManager.getManager(project)
                            .createCachedValue<LightClassDataHolder.ForScript>(stubProvider,false)
                }
            }
        }

        private val cachedValue: CachedValue<ScriptCacheData> = CachedValuesManager.getManager(project).createCachedValue<ScriptCacheData>(
                { CachedValueProvider.Result.create(ScriptCacheData(), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT) },
                false)

        operator fun get(qualifiedName: FqName, searchScope: GlobalSearchScope): CachedValue<LightClassDataHolder.ForScript> {
            synchronized (cachedValue) {
                return cachedValue.value.cache.get(StubCacheKey(qualifiedName, searchScope))
            }
        }

        companion object {
            fun getInstance(project: Project): ScriptStubCache =
                    ServiceManager.getService<ScriptStubCache>(project, ScriptStubCache::class.java)
        }
    }

    val files: Collection<KtFile> = files.toSet() // needed for hashCode

    private val hashCode: Int = computeHashCode()

    private val packageFqName: FqName = scriptClassFqName.parent()

    private val modifierList: PsiModifierList = LightModifierList(manager, KotlinLanguage.INSTANCE, PsiModifier.PUBLIC, PsiModifier.FINAL)

    private val implementsList: LightEmptyImplementsList = LightEmptyImplementsList(manager)

    private val packageClsFile = FakeFileForLightClass(
            files.first(),
            lightClass = { this },
            stub = { lightClassDataCache.value.javaFileStub },
            packageFqName = packageFqName)

    override val kotlinOrigin: KtClassOrObject? get() = null

    val fqName: FqName = scriptClassFqName

    override fun getModifierList() = modifierList

    override fun hasModifierProperty(@NonNls name: String) = modifierList.hasModifierProperty(name)

    override fun isDeprecated() = false

    override fun isInterface() = false

    override fun isAnnotationType() = false

    override fun isEnum() = false

    override fun getContainingClass() = null

    override fun getContainingFile() = packageClsFile

    override fun hasTypeParameters() = false

    override fun getTypeParameters(): Array<PsiTypeParameter> = PsiTypeParameter.EMPTY_ARRAY

    override fun getTypeParameterList() = null

    override fun getDocComment() = null

    override fun getImplementsList() = implementsList

    override fun getImplementsListTypes(): Array<PsiClassType> = PsiClassType.EMPTY_ARRAY

    override fun getInterfaces(): Array<PsiClass> = PsiClass.EMPTY_ARRAY

    override fun getInnerClasses(): Array<PsiClass> = PsiClass.EMPTY_ARRAY

    override fun getOwnInnerClasses(): List<PsiClass> = listOf()

    override fun getAllInnerClasses(): Array<PsiClass> = PsiClass.EMPTY_ARRAY

    override fun getInitializers(): Array<PsiClassInitializer> = PsiClassInitializer.EMPTY_ARRAY

    override fun findInnerClassByName(@NonNls name: String, checkBases: Boolean) = null

    override fun getName() = scriptClassFqName.shortName().asString()

    override fun setName(name: String): PsiElement? {
        for (file in files) {
            val jvmNameEntry = JvmFileClassUtil.findAnnotationEntryOnFileNoResolve(file, JvmFileClassUtil.JVM_NAME_SHORT)

            if (PackagePartClassUtils.getFilePartShortName(file.name) == name) {
                jvmNameEntry?.delete()
                continue
            }

            if (jvmNameEntry == null) {
                val newFileName = PackagePartClassUtils.getFileNameByFacadeName(name)
                val facadeDir = file.parent
                if (newFileName != null && facadeDir != null && facadeDir.findFile(newFileName) == null) {
                    file.name = newFileName
                    continue
                }

                val psiFactory = KtPsiFactory(this)
                val annotationText = "${JvmFileClassUtil.JVM_NAME_SHORT}(\"$name\")"
                val newFileAnnotationList = psiFactory.createFileAnnotationListWithAnnotation(annotationText)
                val annotationList = file.fileAnnotationList
                if (annotationList != null) {
                    annotationList.add(newFileAnnotationList.annotationEntries.first())
                }
                else {
                    val anchor = file.firstChild.siblings().firstOrNull { it !is PsiWhiteSpace && it !is PsiComment }
                    file.addBefore(newFileAnnotationList, anchor)
                }
                continue
            }

            val jvmNameExpression = jvmNameEntry.valueArguments.firstOrNull()?.getArgumentExpression() as? KtStringTemplateExpression
                                    ?: continue
            ElementManipulators.handleContentChange(jvmNameExpression, name)
        }

        return this
    }

    override fun getQualifiedName() = scriptClassFqName.asString()

    override fun isValid() = files.all { it.isValid && PackagePartClassUtils.fileHasTopLevelCallables(it) && scriptClassFqName == it.javaFileFacadeFqName }

    override fun copy() = KtLightClassForScript(manager, scriptClassFqName, lightClassDataCache, files)

    override val lightClassData
        get() = lightClassDataCache.value.findDataForScript(scriptClassFqName)

    override fun getNavigationElement() = files.iterator().next()

    override fun isEquivalentTo(another: PsiElement?): Boolean =
            another is PsiClass && Comparing.equal(another.qualifiedName, qualifiedName)

    override fun getElementIcon(flags: Int): Icon? = throw UnsupportedOperationException("This should be done by JetIconProvider")

    // TODO:
    override fun isInheritor(baseClass: PsiClass, checkDeep: Boolean): Boolean =
            baseClass.qualifiedName == CommonClassNames.JAVA_LANG_OBJECT

    // TODO:
    override fun getSuperClass(): PsiClass? = JavaPsiFacade.getInstance(project).findClass(CommonClassNames.JAVA_LANG_OBJECT, resolveScope)

    override fun getSupers(): Array<PsiClass> = superClass?.let { arrayOf(it) } ?: arrayOf()

    override fun getSuperTypes(): Array<PsiClassType> =
            arrayOf(PsiType.getJavaLangObject(manager, resolveScope))

    override fun hashCode() = hashCode

    private fun computeHashCode(): Int {
        var result = manager.hashCode()
        result = 31 * result + files.hashCode()
        result = 31 * result + scriptClassFqName.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this::class.java != other::class.java) {
            return false
        }

        val lightClass = other as KtLightClassForScript
        if (this === other) return true

        if (this.hashCode != lightClass.hashCode) return false
        if (manager != lightClass.manager) return false
        if (files != lightClass.files) return false
        if (scriptClassFqName != lightClass.scriptClassFqName) return false

        return true
    }

    override fun toString() = "${KtLightClassForScript::class.java.simpleName}:$scriptClassFqName"

    companion object Factory {
        fun createForScript(
                manager: PsiManager,
                classFqName: FqName,
                searchScope: GlobalSearchScope,
                files: Collection<KtFile>
        ): KtLightClassForScript {
            assert(files.isNotEmpty()) { "No files for script $classFqName" }

            val lightClassDataCache = ScriptStubCache.getInstance(manager.project)[classFqName, searchScope]
            return KtLightClassForScript(manager, classFqName, lightClassDataCache, files)
        }
    }

    override val originKind: LightClassOriginKind
        get() = LightClassOriginKind.SOURCE
}
