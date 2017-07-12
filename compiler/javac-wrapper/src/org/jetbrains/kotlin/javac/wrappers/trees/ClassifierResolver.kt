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

package org.jetbrains.kotlin.javac.wrappers.trees

import com.sun.source.tree.Tree
import com.sun.source.util.TreePath
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifier
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ClassifierResolver(private val javac: JavacWrapper) {

    private val cache = hashMapOf<Tree, JavaClassifier?>()

    fun resolve(treePath: TreePath): JavaClassifier? = with (treePath) {
        if (cache.containsKey(leaf)) return cache[leaf]

        return tryToResolve().apply { cache[leaf] = this }
    }

    private val TreePath.enclosingClasses: List<JavaClass>
        get() {
            val outerClasses = filterIsInstance<JCTree.JCClassDecl>()
                    .dropWhile { it.extending == leaf || leaf in it.implementing }
                    .asReversed()
                    .map { it.simpleName.toString() }

            val packageName = compilationUnit.packageName?.toString() ?: "<root>"
            val outermostClassName = outerClasses.firstOrNull() ?: return emptyList()

            val outermostClassId = classId(packageName, outermostClassName)
            var outermostClass = javac.findClass(outermostClassId) ?: return emptyList()

            val classes = arrayListOf<JavaClass>().apply { add(outermostClass) }

            for (it in outerClasses.drop(1)) {
                outermostClass = outermostClass.findInnerClass(Name.identifier(it)) ?: return classes
                classes.add(outermostClass)
            }

            return classes
        }

    private fun TreePath.tryToResolve(): JavaClassifier? {
        val pathSegments = leaf.toString()
                .substringBefore("<")
                .substringAfter("@")
                .split(".")

        val firstSegment = pathSegments.first()

        val asteriskImports = {
            (compilationUnit as JCTree.JCCompilationUnit).imports
                    .mapNotNull {
                        val fqName = it.qualifiedIdentifier.toString()
                        if (fqName.endsWith("*")) {
                            fqName.dropLast(1)
                        }
                        else null
                    }
        }
        val packageName = {
            compilationUnit.packageName?.toString() ?: "<root>"
        }
        val imports = {
            (compilationUnit as JCTree.JCCompilationUnit).imports
                    .mapNotNull {
                        val fqName = it.qualifiedIdentifier.toString()
                        if (fqName.endsWith(".$firstSegment")) {
                            fqName
                        }
                        else null
                    }
        }

        val javaClass = createResolutionScope(enclosingClasses, asteriskImports, packageName, imports).findClass(firstSegment, pathSegments)

        return javaClass ?: tryToResolveTypeParameter()
    }

    private fun TreePath.tryToResolveTypeParameter() =
            flatMap {
                when (it) {
                    is JCTree.JCClassDecl -> it.typarams
                    is JCTree.JCMethodDecl -> it.typarams
                    else -> emptyList<JCTree.JCTypeParameter>()
                }
            }
                    .find { it.toString().substringBefore(" ") == leaf.toString() }
                    ?.let {
                        TreeBasedTypeParameter(it,
                                               javac.getTreePath(it, compilationUnit),
                                               javac)
                    }

    fun createResolutionScope(enclosingClasses: List<JavaClass>,
                              asteriskImports: () -> List<String>,
                              packageName: () -> String,
                              imports: () -> List<String>): Scope {

        val globalScope = { GlobalScope(javac) }
        val importOnDemandScope = { ImportOnDemandScope(javac, globalScope, asteriskImports) }
        val packageScope = { PackageScope(javac, importOnDemandScope, packageName) }
        val singleTypeImportScope = { SingleTypeImportScope(javac, packageScope, imports) }
        val currentClassAndInnerScope = CurrentClassAndInnerScope(javac, singleTypeImportScope, enclosingClasses)

        return currentClassAndInnerScope
    }

}

interface Scope {
    val parent: Scope?
    val javac: JavacWrapper

    fun findClass(name: String, pathSegments: List<String>): JavaClass?
}

private class GlobalScope(override val javac: JavacWrapper) : Scope {

    override val parent: Scope?
        get() = null

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        findByFqName(pathSegments, javac)?.let { return it }

        return findJavaOrKotlinClass(classId("java.lang", name), javac)?.let { javaClass ->
            getJavaClassFromPathSegments(javaClass, pathSegments)
        }
    }

}

private class ImportOnDemandScope(override val javac: JavacWrapper,
                                  private val scope: () -> Scope?,
                                  private val asteriskImports: () -> List<String>) : Scope {

    override val parent: Scope? get() = scope()

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        asteriskImports()
                .toSet()
                .mapNotNull { findByFqName("$it$name".split("."), javac) }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    return it.singleOrNull()?.let { javaClass ->
                        getJavaClassFromPathSegments(javaClass, pathSegments)
                    }
                }

        return parent?.findClass(name, pathSegments)
    }

}

private class PackageScope(override val javac: JavacWrapper,
                           private val scope: () -> Scope?,
                           private val packageName: () -> String) : Scope {

    override val parent: Scope? get() = scope()

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        findJavaOrKotlinClass(classId(packageName(), name), javac)?.let { javaClass ->
            return getJavaClassFromPathSegments(javaClass, pathSegments)
        }

        return parent?.findClass(name, pathSegments)
    }

}

private class SingleTypeImportScope(override val javac: JavacWrapper,
                                    private val scope: () -> Scope?,
                                    private val imports: () -> List<String>) : Scope {

    override val parent: Scope? get() = scope()

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        val imports = imports().toSet().takeIf { it.isNotEmpty() } ?: return parent?.findClass(name, pathSegments)

        imports.singleOrNull() ?: return null

        return findByFqName(imports.first().split("."), javac)
                ?.let { javaClass -> getJavaClassFromPathSegments(javaClass, pathSegments) }
    }

}

private class CurrentClassAndInnerScope(override val javac: JavacWrapper,
                                        private val scope: () -> Scope?,
                                        private val enclosingClasses: List<JavaClass>) : Scope {

    override val parent: Scope? get() = scope()

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        enclosingClasses.forEach {
            it.findInner(Name.identifier(name))?.let { javaClass ->
                return getJavaClassFromPathSegments(javaClass, pathSegments)
            }
        }

        return parent?.findClass(name, pathSegments)
    }

}

private fun getJavaClassFromPathSegments(javaClass: JavaClass,
                                         pathSegments: List<String>) =
        if (pathSegments.size == 1) {
            javaClass
        }
        else {
            javaClass.findInner(pathSegments.drop(1))
        }

private fun findByFqName(pathSegments: List<String>, javac: JavacWrapper): JavaClass? {
    pathSegments.forEachIndexed { index, _ ->
        if (index == pathSegments.lastIndex) return null
        val packageFqName = pathSegments.dropLast(index + 1).joinToString(separator = ".")
        findPackage(packageFqName, javac)?.let { pack ->
            val className = pathSegments.takeLast(index + 1)
            return findJavaOrKotlinClass(ClassId(pack, Name.identifier(className.first())), javac)?.let { javaClass ->
                getJavaClassFromPathSegments(javaClass, className)
            }
        }
    }

    return null
}

private fun findPackage(packageName: String, javac: JavacWrapper): FqName? {
    val fqName = FqName(packageName)
    javac.hasKotlinPackage(fqName)?.let { return it }

    return javac.findPackage(fqName)?.fqName
}

private fun findJavaOrKotlinClass(classId: ClassId, javac: JavacWrapper) = javac.findClass(classId) ?: javac.getKotlinClassifier(classId)

fun classId(packageName: String = "<root>", className: String) =
        if (packageName != "<root>")
            ClassId(FqName(packageName), Name.identifier(className))
        else
            ClassId(FqName.ROOT, FqName(className), false)

fun JavaClass.findInner(pathSegments: List<String>): JavaClass? =
        pathSegments.fold(this) { javaClass, it -> javaClass.findInner(Name.identifier(it)) ?: return null }

fun JavaClass.findInner(name: Name): JavaClass? {
    findInnerClass(name)?.let { return it }

    supertypes.mapNotNull { it.classifier as? JavaClass }
            .forEach { javaClass -> javaClass.findInner(name)?.let { return it } }

    return null
}