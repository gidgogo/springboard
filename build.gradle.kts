// 최상위 빌드 파일: 플러그인 버전만 선언하고 각 모듈에서 적용한다.
// 버전은 gradle/libs.versions.toml 한 곳에서 관리한다.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
