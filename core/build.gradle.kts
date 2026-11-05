import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 안드로이드에 의존하지 않는 순수 Kotlin 모듈.
// 주행 상태 판단, 데모 시나리오, 음성 명령 매칭, Gleo 스키마/콜백 같은 "로직"을 여기에 두면
// 에뮬레이터 없이 ./gradlew :core:test 로 바로 검증할 수 있다.
plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Flow 타입이 공개 API에 나오므로 api 로 노출한다.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    // GleoSchemaRulesTest 가 docs/templates 의 예시 스키마를 읽는다.
    systemProperty("springboard.rootDir", rootProject.projectDir.absolutePath)
}
