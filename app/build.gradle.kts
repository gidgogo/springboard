import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// ---------------------------------------------------------------------------
// 릴리스 서명: 저장소 루트의 keystore.properties (git 에 올리지 않는다, .gitignore 처리됨)
//   storeFile=keystore/springboard-release.jks   (저장소 루트 기준 상대경로 또는 절대경로)
//   storePassword=...
//   keyAlias=springboard
//   keyPassword=...
// 파일이 없으면 release APK 는 서명되지 않은 채(app-release-unsigned.apk) 만들어진다.
// 자세한 절차: docs/04-dev/build-and-release.md
// ---------------------------------------------------------------------------
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val hasReleaseSigning = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    .all { !keystoreProperties.getProperty(it).isNullOrBlank() }

android {
    namespace = "com.springboard.app"
    compileSdk = 35

    // AAOS Car API (CarUxRestrictionsManager, CarPropertyManager). 실제 구현은 차량(에뮬레이터)이 런타임에 제공한다.
    useLibrary("android.car")

    defaultConfig {
        applicationId = "com.springboard.app"
        // Pleos 요구사항: minSdk 28 이상 (차량 사이버보안 요건: 서명 키 로테이션·리니지)
        minSdk = 28
        targetSdk = 35
        versionCode = providers.gradleProperty("app.versionCode").get().toInt()
        versionName = providers.gradleProperty("app.versionName").get()

        buildConfigField("String", "PLEOS_VEHICLE_SDK", "\"${libs.versions.pleosVehicle.get()}\"")
        buildConfigField("String", "PLEOS_NAVI_HELPER_SDK", "\"${libs.versions.pleosNaviHelper.get()}\"")
        buildConfigField("String", "PLEOS_TTS_SDK", "\"${libs.versions.pleosTextToSpeech.get()}\"")
        buildConfigField("String", "PLEOS_STT_SDK", "\"${libs.versions.pleosSpeechToText.get()}\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Pleos 보안 가이드: 배포 빌드는 정식 서명 + 디버깅 비활성화
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // 초기 단계: 리포트만 남기고 빌드는 막지 않는다. CI 가 리포트를 아티팩트로 올린다.
        // 제출 전에는 docs/05-submission/submission-checklist.md 에 따라 리포트를 한 번 정리한다.
        abortOnError = false
        checkReleaseBuilds = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Pleos Connect SDK — 쓰지 않는 SDK 는 지우고, 해당 권한도 AndroidManifest.xml 에서 지운다.
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.text.to.speech)
    implementation(libs.pleos.speech.to.text)

    testImplementation(libs.junit)
}

// ---------------------------------------------------------------------------
// 공모전 제출용 APK 내보내기
//   ./gradlew :app:exportReleaseApk
//   → apk/<submission.teamName>_<submission.appName>_release.apk (gradle.properties 에서 이름 설정)
// 제출 규칙: 소스+APK 를 한 저장소에, APK 는 apk/ 폴더에, "실행 가능한 release 빌드".
// ---------------------------------------------------------------------------
val submissionTeam = providers.gradleProperty("submission.teamName").get()
val submissionApp = providers.gradleProperty("submission.appName").get()

tasks.register<Copy>("exportReleaseApk") {
    group = "submission"
    description = "서명된 release APK 를 apk/${submissionTeam}_${submissionApp}_release.apk 로 복사한다."
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("*-release.apk")
    }
    into(rootProject.layout.projectDirectory.dir("apk"))
    rename { "${submissionTeam}_${submissionApp}_release.apk" }
    doFirst {
        if (!hasReleaseSigning) {
            throw GradleException(
                "keystore.properties 가 없어 release APK 에 서명할 수 없습니다. " +
                    "docs/04-dev/build-and-release.md 의 '릴리스 서명 키 만들기'를 먼저 하세요.",
            )
        }
    }
}
