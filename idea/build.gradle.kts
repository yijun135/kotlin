
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
    compile(ideaSdkDeps("IntelliLang", subdir = "plugins/IntelliLang/lib"))
    compile(ideaSdkDeps("testng", "testng-plugin", subdir = "plugins/testng/lib"))
    compile(ideaSdkDeps("copyright", subdir = "plugins/copyright/lib"))
    compile(ideaSdkDeps("properties", subdir = "plugins/properties/lib"))
    compile(ideaSdkDeps("java-i18n", subdir = "plugins/java-i18n/lib"))
    compile(ideaSdkDeps("java-decompiler", subdir = "plugins/java-decompiler/lib"))
    compile(ideaSdkDeps("Groovy", subdir = "plugins/Groovy/lib"))
    compile(ideaSdkDeps("maven", "maven-server-api", subdir = "plugins/maven/lib"))
    compile(ideaSdkDeps("coverage", subdir = "plugins/coverage/lib"))
    compile(preloadedDeps("markdown", "kotlinx-coroutines-core"))
    testCompile(project(":kotlin-test:kotlin-test-junit"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":idea:idea-test-framework")) { isTransitive = false }
    testCompile(ideaSdkDeps("gradle-base-services", "gradle-tooling-extension-impl", "gradle-wrapper", subdir = "plugins/gradle/lib"))
    testCompile(ideaSdkDeps("groovy-all"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaPluginDeps("idea-junit", "resources_en", plugin = "junit"))
    testRuntime(ideaPluginDeps("IntelliLang", plugin = "IntelliLang"))
    testRuntime(ideaPluginDeps("jcommander", "testng", "testng-plugin", "resources_en", plugin = "testng"))
    testRuntime(ideaPluginDeps("copyright", plugin = "copyright"))
    testRuntime(ideaPluginDeps("properties", "resources_en", plugin = "properties"))
    testRuntime(ideaPluginDeps("java-i18n", plugin = "java-i18n"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "gradle"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "Groovy"))
    testRuntime(ideaPluginDeps("coverage", "jacocoant", plugin = "coverage"))
    testRuntime(ideaPluginDeps("java-decompiler", plugin = "java-decompiler"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "maven"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "android"))
    testRuntime(preloadedDeps("uast-common", "uast-java"))
    // deps below are test runtime deps, but made test compile to split compilation and running to reduce mem req
    testCompile(project(":plugins:android-extensions-compiler"))
    testCompile(project(":plugins:android-extensions-idea"))
    testCompile(project(":plugins:allopen-ide")) { isTransitive = false }
    testCompile(project(":plugins:allopen-cli"))
    testCompile(project(":plugins:noarg-ide")) { isTransitive = false }
    testCompile(project(":plugins:noarg-cli"))
    testCompile(project(":plugins:annotation-based-compiler-plugins-ide-support")) { isTransitive = false }
    testCompile(project(":plugins:sam-with-receiver-ide")) { isTransitive = false }
    testCompile(project(":plugins:sam-with-receiver-cli"))
    testCompile(project(":idea:idea-android")) { isTransitive = false }
    testCompile(project(":plugins:lint")) { isTransitive = false }
    testCompile(project(":plugins:uast-kotlin"))
    (rootProject.extra["compilerModules"] as Array<String>).forEach {
        testCompile(project(it))
    }
    testCompile(project(":prepare:compiler", configuration = "default"))

    buildVersion()
}

configureKotlinProjectSources("src",
                              "idea-maven/src",
                              "idea-completion/src",
                              "idea-live-templates/src",
                              "idea-repl/src")
configure<JavaPluginConvention> {
    sourceSets["main"].apply {
        resources {
            srcDir(File(projectDir, "resources"))
                    .include("**")
            srcDir(File(projectDir, "src"))
                    .include("META-INF/**",
                             "**/*.properties")
        }
    }
}
configureKotlinProjectTests("idea/tests",
                            "idea/idea-maven/test",
                            "idea/idea-completion/tests",
                            "j2k/tests",
                            "eval4j/test",
                            sourcesBaseDir = rootDir)

tasks.withType<Test> {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    jvmArgs("-ea", "-XX:+HeapDumpOnOutOfMemoryError", "-Xmx1200m", "-XX:+UseCodeCacheFlushing", "-XX:ReservedCodeCacheSize=128m", "-Djna.nosys=true")
    maxHeapSize = "1200m"
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
//    forkEvery = 100
    testLogging {
//        events = setOf(TestLogEvent.FAILED)
//        showStackTraces = true
//        showCauses = true
//        exceptionFormat = TestExceptionFormat.FULL
//        showStandardStreams = false
    }
    ignoreFailures = true
}

testsJar {}

fixKotlinTaskDependencies()

