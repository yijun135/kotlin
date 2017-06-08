
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":core"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:cli-common"))
    compile(project(":compiler:daemon-client"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:frontend.script"))
    compile(project(":js:js.frontend"))
    compile(project(":js:js.serializer"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:util"))
    compile(project(":compiler:compiler-runner"))
    compile(project(":compiler:plugin-api"))
    compile(project(":eval4j"))
    compile(project(":j2k"))
    compile(project(":idea:formatter"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:ide-common"))
    compile(project(":idea:idea-jps-common"))
    compile(project(":idea:kotlin-gradle-tooling"))
    compile(project(":plugins:uast-kotlin"))
    compile(project(":plugins:uast-kotlin-idea"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    compile(ideaSdkDeps("openapi", "idea", "velocity", "boot", "gson", "swingx-core", "jsr305"))
    compile(ideaSdkDeps("gradle-tooling-api", "gradle", subdir = "plugins/gradle/lib"))
    compile(ideaSdkDeps("idea-junit", subdir = "plugins/junit/lib"))
    compile(ideaSdkDeps("IntelliLang", subdir = "plugins/intelliLang/lib"))
    compile(ideaSdkDeps("testng", "testng-plugin", subdir = "plugins/testng/lib"))
    compile(ideaSdkDeps("copyright", subdir = "plugins/copyright/lib"))
    compile(ideaSdkDeps("properties", subdir = "plugins/properties/lib"))
    compile(ideaSdkDeps("java-i18n", subdir = "plugins/java-i18n/lib"))
    compile(ideaSdkDeps("java-decompiler", subdir = "plugins/java-decompiler/lib"))
    compile(ideaSdkDeps("Groovy", subdir = "plugins/Groovy/lib"))
    compile(ideaSdkDeps("maven", "maven-server-api", subdir = "plugins/maven/lib"))
    compile(ideaSdkDeps("coverage", subdir = "plugins/coverage/lib"))
    compile(preloadedDeps("markdown", "kotlinx-coroutines-core"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":idea:idea-test-framework"))
    testCompile(ideaSdkDeps("gradle-base-services", "gradle-tooling-extension-impl", "gradle-wrapper", subdir = "plugins/gradle/lib"))
    testCompile(ideaSdkDeps("groovy-all"))
    testRuntime(project(":prepare:compiler", configuration = "default"))
    testRuntime(project(":plugins:android-extensions-compiler"))
    testRuntime(project(":plugins:android-extensions-idea"))
//    testRuntime(fileTree(File($rootDir, "ideaSDK/lib")))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-i18n/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/gradle/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/junit/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/intelliLang/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/testng/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/copyright/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-decompiler/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/Groovy/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/maven/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/coverage/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/android/lib"))
    buildVersion()
}

configureKotlinProjectSources("src",
                              "idea-maven/src",
                              "idea-completion/src",
                              "idea-live-templates/src",
                              "idea-repl/src")
configureKotlinProjectTests("tests",
                            "idea-completion/tests")

tasks.withType<Test> {
    jvmArgs("-ea", "-XX:+HeapDumpOnOutOfMemoryError", "-Xmx1250m", "-XX:+UseCodeCacheFlushing", "-XX:ReservedCodeCacheSize=128m", "-Djna.nosys=true")
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
}

fixKotlinTaskDependencies()

