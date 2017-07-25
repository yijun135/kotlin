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

package org.jetbrains.kotlin.idea.core.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext


public object EDT : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !ApplicationManager.getApplication().isDispatchThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val project = context[ProjectElement.Key]?.project
        if (project?.isDisposed == true) {
            return
        }

        val modalityState = context[ModalityStateElement.Key]?.modalityState ?: ModalityState.defaultModalityState()
        ApplicationManager.getApplication().invokeLater(block.abortIfDisposed(project), modalityState)
    }

    private fun Runnable.abortIfDisposed(project: Project?): Runnable {
        if (project == null) return this

        return Runnable {
            if (project.isDisposed) return@Runnable
            else run()
        }
    }

    class ModalityStateElement(val modalityState: ModalityState) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<ModalityStateElement>
    }

    class ProjectElement(val project: Project) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<ProjectElement>
    }

    operator fun plus(project: Project): CoroutineContext = this + EDT.ProjectElement(project)
}

