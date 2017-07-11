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

package org.jetbrains.kotlin.javac

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.load.java.structure.impl.VirtualFileBoundJavaClass
import org.jetbrains.kotlin.name.ClassId

class KotlinClassifiersCache(sourceFiles: Collection<KtFile>,
                             private val javac: JavacWrapper) {

    private val kotlinClasses: Map<ClassId?, KtClassOrObject?> =
            sourceFiles.flatMap { ktFile ->
                ktFile.declarations
                        .filterIsInstance<KtClassOrObject>()
                        .map { it.computeClassId() to it }
            }.toMap()

    private val classifiers = hashMapOf<ClassId, JavaClass>()
    private val supertypesCache = hashMapOf<KtClassOrObject, JavaClass?>()

    fun getKotlinClassifier(classId: ClassId) = classifiers[classId] ?: createClassifier(classId)

    fun resolveSupertype(name: String,
                         classOrObject: KtClassOrObject,
                         javac: JavacWrapper): JavaClass? {
        if (supertypesCache.containsKey(classOrObject)) {
            return supertypesCache[classOrObject]
        }
        val pathSegments = name.split(".")
        val firstSegment = pathSegments.first()

        val ktFile = classOrObject.containingKtFile

        val enclosingClasses = classOrObject.enclosingClasses
        val asteriskImports = {
            ktFile.importDirectives
                    .mapNotNull {
                        if (it.text.endsWith("*")) {
                            it.importedFqName!!.asString()
                        }
                        else null
                    }
        }
        val packageName = {
            ktFile.packageFqName.asString()
        }
        val imports = {
            ktFile.importDirectives
                    .mapNotNull {
                        if (it.text.endsWith(".$firstSegment")) {
                            it.importedFqName!!.asString()
                        }
                        else null
                    }
        }

        val resolutionScope = javac.classifierResolver.createResolutionScope(enclosingClasses, asteriskImports, packageName, imports)

        return resolutionScope.findClass(firstSegment, pathSegments).apply { supertypesCache[classOrObject] = this }
    }

    fun createMockKotlinClassifier(classifier: KtClassOrObject,
                                   classId: ClassId) = MockKotlinClassifier(classId.asSingleFqName(),
                                                                            classifier,
                                                                            classifier.typeParameters.isNotEmpty(),
                                                                            this,
                                                                            javac)
            .apply { classifiers[classId] = this }

    private fun createClassifier(classId: ClassId): JavaClass? {
        if (classId.isNestedClass) {
            classifiers[classId]?.let { return it }
            val pathSegments = classId.relativeClassName.pathSegments().map { it.asString() }
            val outerClassId = ClassId(classId.packageFqName, Name.identifier(pathSegments.first()))
            var outerClass: JavaClass = kotlinClasses[outerClassId]?.let { createMockKotlinClassifier(it, outerClassId) } ?: return null

            pathSegments.drop(1).forEach {
                outerClass = outerClass.findInnerClass(Name.identifier(it)) ?: return null
            }

            return outerClass.apply { classifiers[classId] = this }
        }

        val kotlinClassifier = kotlinClasses[classId] ?: return null

        return createMockKotlinClassifier(kotlinClassifier, classId)
    }

    private val KtClassOrObject.enclosingClasses: List<JavaClass>
        get() {
            val classOrObjects = arrayListOf<KtClassOrObject>()

            var outerClass: KtClassOrObject? = this.containingClassOrObject ?: return emptyList()

            while (outerClass != null) {
                classOrObjects.add(outerClass)
                outerClass = outerClass.containingClassOrObject
            }

            return classOrObjects.reversed().mapNotNull { it.computeClassId()?.let { createClassifier(it) } }
        }

}

class MockKotlinClassifier(override val fqName: FqName,
                           private val classOrObject: KtClassOrObject,
                           val hasTypeParameters: Boolean,
                           private val cache: KotlinClassifiersCache,
                           private val javac: JavacWrapper) : VirtualFileBoundJavaClass {

    override val isAbstract: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val isStatic: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val isFinal: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val visibility: Visibility
        get() = throw UnsupportedOperationException("Should not be called")

    override val typeParameters: List<JavaTypeParameter>
        get() = throw UnsupportedOperationException("Should not be called")

    override val supertypes: Collection<JavaClassifierType>
        get() = classOrObject.superTypeListEntries
                .map { superTypeListEntry ->
                    val userType = superTypeListEntry.typeAsUserType
                    arrayListOf<String>().apply {
                        userType?.referencedName?.let { add(it) }
                        var qualifier = userType?.qualifier
                        while (qualifier != null) {
                            qualifier.referencedName?.let { add(it) }
                            qualifier = qualifier.qualifier
                        }
                    }.reversed().joinToString(separator = ".") { it }
                }
                .mapNotNull { cache.resolveSupertype(it, classOrObject, javac) }
                .map { MockKotlinClassifierType(it) }

    val innerClasses: Collection<JavaClass>
        get() = classOrObject.declarations.filterIsInstance<KtClassOrObject>()
                .mapNotNull { nestedClassOrObject ->
                    nestedClassOrObject.computeClassId()?.let {
                        cache.createMockKotlinClassifier(nestedClassOrObject, it)
                    }
                }

    override val outerClass: JavaClass?
        get() = throw UnsupportedOperationException("Should not be called")

    override val isInterface: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val isAnnotationType: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val isEnum: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val lightClassOriginKind
        get() = LightClassOriginKind.SOURCE

    override val virtualFile: VirtualFile?
        get() = null

    override val methods: Collection<JavaMethod>
        get() = throw UnsupportedOperationException("Should not be called")

    override val fields: Collection<JavaField>
        get() = throw UnsupportedOperationException("Should not be called")

    override val constructors: Collection<JavaConstructor>
        get() = throw UnsupportedOperationException("Should not be called")

    override val name
        get() = fqName.shortNameOrSpecial()

    override val annotations
        get() = throw UnsupportedOperationException("Should not be called")

    override val isDeprecatedInJavaDoc: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override fun isFromSourceCodeInScope(scope: SearchScope) = true

    override fun findAnnotation(fqName: FqName) =
            throw UnsupportedOperationException("Should not be called")

    override val innerClassNames
        get() = innerClasses.map(JavaClass::name)

    override fun findInnerClass(name: Name) =
            innerClasses.find { it.name == name }

}

class MockKotlinClassifierType(override val classifier: JavaClassifier) : JavaClassifierType {

    override val typeArguments: List<JavaType>
        get() = throw UnsupportedOperationException("Should not be called")

    override val isRaw: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

    override val annotations: Collection<JavaAnnotation>
        get() = throw UnsupportedOperationException("Should not be called")

    override val classifierQualifiedName: String
        get() = throw UnsupportedOperationException("Should not be called")

    override val presentableText: String
        get() = throw UnsupportedOperationException("Should not be called")

    override fun findAnnotation(fqName: FqName) =
            throw UnsupportedOperationException("Should not be called")

    override val isDeprecatedInJavaDoc: Boolean
        get() = throw UnsupportedOperationException("Should not be called")

}

private fun KtClassOrObject.computeClassId(): ClassId? =
        containingClassOrObject?.computeClassId()?.createNestedClassId(nameAsSafeName) ?: fqName?.let { ClassId.topLevel(it) }











