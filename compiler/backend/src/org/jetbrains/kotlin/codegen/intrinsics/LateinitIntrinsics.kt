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

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

object LateinitIsInitialized : IntrinsicPropertyGetter() {
    override fun generate(resolvedCall: ResolvedCall<*>?, codegen: ExpressionCodegen, returnType: Type, receiver: StackValue): StackValue? {
        val value = getStackValue(resolvedCall ?: return null, codegen) ?: return null
        return StackValue.compareWithNull(value, Opcodes.IFNULL)
    }
}

object LateinitReset : IntrinsicMethod() {
    override fun toCallable(fd: FunctionDescriptor, isSuper: Boolean, resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable =
            generate(fd, resolvedCall, codegen) ?: super.toCallable(fd, isSuper, resolvedCall, codegen)

    private fun generate(fd: FunctionDescriptor, resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable? {
        val value = getStackValue(resolvedCall, codegen) ?: return null
        val callableMethod = codegen.state.typeMapper.mapToCallableMethod(fd, false)
        return LateinitResetIntrinsicCallable(value, callableMethod)
    }
}

private class LateinitResetIntrinsicCallable(private val value: StackValue, method: CallableMethod) : IntrinsicCallable(
        method.returnType, method.valueParameterTypes, method.dispatchReceiverType, method.extensionReceiverType
), IntrinsicWithSpecialReceiver {
    override fun invokeIntrinsic(v: InstructionAdapter) {
        value.store(StackValue.constant(null, value.type), v)
    }
}

private fun getStackValue(resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): StackValue? {
    val expression =
            (resolvedCall.extensionReceiver as? ExpressionReceiver)?.expression as? KtCallableReferenceExpression ?: return null

    // TODO: support properties imported from objects as soon as KT-18982 is fixed
    val receiver = expression.receiverExpression?.let(codegen::gen) ?: return null

    val target = expression.callableReference.getResolvedCallWithAssert(codegen.bindingContext).resultingDescriptor
    return codegen.intermediateValueForProperty(target as PropertyDescriptor, true, false, null, false, receiver, null, true)
}
