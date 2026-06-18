plugins {
    alias(libs.plugins.android.application)
    id("jacoco")
}

android {
    namespace = "com.example.matonique"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.matonique"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.media:media:1.7.1")

    implementation("androidx.room:room-runtime:2.6.1")
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.rules)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    debugImplementation("androidx.fragment:fragment-testing:1.8.5") // Version plus récente

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation("org.mockito:mockito-core:5.11.0")

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation(libs.test.core)
    testImplementation(libs.ext.junit)
    testImplementation(libs.espresso.core)
    testImplementation("org.robolectric:robolectric:4.11.1")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
}

jacoco {
    toolVersion = "0.8.11"
}

// Config speciale pour que Jacoco voit ce que fait Robolectric
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // On vire les trucs inutiles du rapport (R.class, BuildConfig, etc)
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/androidx/**/*.*"
    )

    val debugTree = fileTree("${project.buildDir}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))

    // On va chercher les resultats partout ou ils peuvent etre
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("jacoco/testDebugUnitTest.exec")
    })
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "androidx.test" && requested.name == "core") {
                useVersion("1.7.0")
            }
            if (requested.group == "androidx.test.espresso" && requested.name == "espresso-core") {
                useVersion("3.7.0") // <-- On force ici la version 3.7.0 qui corrige ce crash
            }
        }
    }
}