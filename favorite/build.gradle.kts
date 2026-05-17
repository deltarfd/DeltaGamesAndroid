plugins {
    alias(libs.plugins.android.dynamic.feature)
    jacoco
}

android {
    namespace = "com.deltarfd.deltagamesandroid.favorite"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.extensions.configure(JacocoTaskExtension::class.java) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// â”€â”€ Jacoco coverage report â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    dependsOn(":app:testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file(
                "reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml"
            )
        )
    }
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*",
        "**/*_MembersInjector.*", "**/*Module*.*",
        "**/entity/**", "**/model/**", "**/response/**",
        "**/databinding/**", "**/android/databinding/**"
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug") {
        exclude(fileFilter)
    }
    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val modernKotlinDebugTree = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(fileFilter)
    }
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree, modernKotlinDebugTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        }
    )
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)

    // Glide
    implementation(libs.glide)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.fragment.testing)
    debugImplementation(libs.androidx.fragment.testing.manifest)
}

tasks.withType<Test> {
    jvmArgs("-noverify")
}

tasks.register("fixRobolectricManifest") {
    dependsOn("processDebugUnitTestManifest")
    doLast {
        fileTree("build/intermediates").matching {
            include("**/AndroidManifest.xml")
        }.forEach { file ->
            var content = file.readText()
            if (content.contains("dist:module")) {
                content = content.replace(Regex("<dist:module.*?</dist:module>", RegexOption.DOT_MATCHES_ALL), "")
            }
            content = content.replace("xmlns:dist=\"http://schemas.android.com/apk/distribution\"", "")
            content = content.replace(Regex("split=\"[^\"]*\""), "")
            file.writeText(content)
        }
    }
}

tasks.withType<Test>().configureEach {
    dependsOn("fixRobolectricManifest")
}

