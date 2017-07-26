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

package org.jetbrains.kotlin.idea.replacementFor

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.utils.SmartList

private typealias ResolvedPatternCheck = (BindingContext) -> Boolean

//TODO: where to check that pattern has all parameters of the callable included?
class PatternMatcher(
        private val callable: CallableDescriptor,
        private val pattern: KtExpression,
        private val analyzePattern: () -> BindingContext
) {
    private val parametersByName: Map<String, ValueParameterDescriptor> = callable.valueParameters.associateBy { it.name.asString() }

    //TODO: type arguments
    class Match(val arguments: Map<ParameterDescriptor, KtExpression>) {
        fun isEmpty() = arguments.isEmpty()

        companion object {
            val EMPTY = Match(emptyMap())
        }
    }

    fun matchExpression(expression: KtExpression, bindingContext: BindingContext): Match? {
        val resolvedPatternChecks = SmartList<ResolvedPatternCheck>()
        val match = matchExpressionPart(expression, pattern, bindingContext, resolvedPatternChecks) ?: return null
        if (resolvedPatternChecks.isEmpty()) return match
        val patternBindingContext = analyzePattern()
        return match.takeIf { resolvedPatternChecks.all { it(patternBindingContext) } }
    }

    private fun matchExpressionPart(
            part: PsiElement,
            patternPart: PsiElement,
            bindingContext: BindingContext,
            resolvedPatternChecks: MutableList<ResolvedPatternCheck>
    ): Match? {
        when (patternPart) {
            is KtSimpleNameExpression -> return matchToSimpleName(part, patternPart, bindingContext, resolvedPatternChecks)

            is KtThisExpression -> {
                if (part !is KtExpression) return null
                if (patternPart.labelQualifier != null) return null //TODO: can we support this at all?
                val receiverParameter = callable.extensionReceiverParameter ?: callable.dispatchReceiverParameter ?: return null /*TODO?*/
                return Match(mapOf(receiverParameter to part))
            }
/*
            //TODO: qualifiers
            is KtDotQualifiedExpression -> {
                if ()
            }
*/
        }

        return matchToNonSpecificElement(part, patternPart, bindingContext, resolvedPatternChecks)
    }

    private fun matchToSimpleName(
            part: PsiElement,
            patternPart: KtSimpleNameExpression,
            bindingContext: BindingContext,
            resolvedPatternChecks: MutableList<ResolvedPatternCheck>
    ): Match? {
        if (part !is KtExpression) return null

        val patternName = patternPart.getReferencedName()
        val parameter = parametersByName[patternName] //TODO: what if name is shadowed inside the pattern (can we ignore such exotic case?)
        if (parameter != null) {
            return Match(mapOf(parameter to part))
        }

        if (part !is KtSimpleNameExpression || part.getReferencedName() != patternName) return null

        resolvedPatternChecks.add { patternBindingContext ->
            val target = bindingContext[BindingContext.REFERENCE_TARGET, part]
            val patternTarget = patternBindingContext[BindingContext.REFERENCE_TARGET, patternPart]
            target == patternTarget // TODO: will not be equal if substituted!
        }

        return Match.EMPTY
    }

    private fun matchToNonSpecificElement(
            part: PsiElement,
            patternPart: PsiElement,
            bindingContext: BindingContext,
            resolvedPatternChecks: MutableList<ResolvedPatternCheck>
    ): Match? {
        val elementType = part.node.elementType
        val patternElementType = patternPart.node.elementType
        if (elementType != patternElementType) return null

        if (patternElementType is KtToken) {
            return if (part.text == patternPart.text) Match.EMPTY else null
        }

        val children = part.childrenToMatch()
        val patternChildren = patternPart.childrenToMatch()
        if (children.size != patternChildren.size) return null

        var match = Match.EMPTY
        for ((child, patternChild) in children.zip(patternChildren)) {
            val childMatch = matchExpressionPart(child, patternChild, bindingContext, resolvedPatternChecks) ?: return null
            match = match.add(childMatch) ?: return null
        }

        return match
    }

    private fun PsiElement.childrenToMatch(): Collection<PsiElement> {
        return allChildren.filterNot { it is PsiWhiteSpace || it is PsiComment }.toList()
    }

    private fun Match.add(partMatch: Match): Match? {
        if (partMatch.isEmpty()) return this
        if (this.isEmpty()) return partMatch
        val newArguments = HashMap(arguments) //TODO: too much new maps created
        for ((parameter, value) in partMatch.arguments) {
            val currentValue = arguments[parameter]
            if (currentValue == null) {
                newArguments.put(parameter, value)
            }
            else {
                //TODO: compare expressions structurally and using bindingContext(?)
                if (currentValue.text != value.text) return null
            }
        }
        return Match(newArguments)
    }
}
