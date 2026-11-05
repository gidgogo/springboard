# 사용한 오픈소스 · SDK 고지

공모전 이용약관(제7조 7항)에 따라 사용한 오픈소스의 출처와 라이선스를 밝힌다.
의존성을 추가하거나 빼면 이 표를 함께 고친다. (빌드에 포함되는 전체 목록 확인: `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`)

## 앱에 포함되는 것

| 구성 요소 | 용도 | 라이선스 | 출처 |
|---|---|---|---|
| AndroidX Core KTX | Android 확장 | Apache License 2.0 | https://developer.android.com/jetpack/androidx/releases/core |
| AndroidX Lifecycle (runtime-ktx, runtime-compose, viewmodel-compose) | 생명주기·ViewModel | Apache License 2.0 | https://developer.android.com/jetpack/androidx/releases/lifecycle |
| AndroidX Activity Compose | Compose 진입점·결과 API | Apache License 2.0 | https://developer.android.com/jetpack/androidx/releases/activity |
| Jetpack Compose (UI, Foundation, Material 3, Tooling Preview) | 화면 | Apache License 2.0 | https://developer.android.com/jetpack/androidx/releases/compose |
| Kotlin Standard Library | 언어 런타임 | Apache License 2.0 | https://github.com/JetBrains/kotlin |
| kotlinx.coroutines | 비동기·Flow | Apache License 2.0 | https://github.com/Kotlin/kotlinx.coroutines |
| kotlinx.serialization (JSON) | Gleo 스키마·콜백 JSON | Apache License 2.0 | https://github.com/Kotlin/kotlinx.serialization |
| Pleos Connect SDK — Vehicle, NaviHelper, TextToSpeech, SpeechToText | 차량 데이터·내비·음성 | Pleos Playground 서비스 이용약관 | https://pleos.ai/playground · https://nexus-playground.pleos.ai/repository/maven-releases/ |
| Pleos SDK가 함께 가져오는 라이브러리 (예: Dagger/Hilt) | SDK 내부 의존 | 각 라이브러리 라이선스 (Dagger: Apache License 2.0) | 위 dependencies 명령으로 확인 |
| AAOS Car API (`android.car`) | 주행 규제·차량 속성 | 플랫폼 제공 (Android Open Source Project, Apache License 2.0) | https://source.android.com/docs/automotive |

## 빌드·테스트에만 쓰는 것 (앱에 포함되지 않음)

| 구성 요소 | 라이선스 | 출처 |
|---|---|---|
| Gradle Wrapper | Apache License 2.0 | https://gradle.org |
| Android Gradle Plugin | Apache License 2.0 | https://developer.android.com/build |
| Kotlin Gradle Plugin / Compose Compiler Plugin | Apache License 2.0 | https://github.com/JetBrains/kotlin |
| JUnit 4 | Eclipse Public License 1.0 | https://junit.org/junit4/ |
| kotlinx-coroutines-test | Apache License 2.0 | https://github.com/Kotlin/kotlinx.coroutines |
| GitHub Actions: actions/checkout, actions/setup-java, actions/upload-artifact | MIT | https://github.com/actions |
| GitHub Actions: gradle/actions (basic caching 사용) | MIT | https://github.com/gradle/actions |

## 데이터 · 콘텐츠

| 데이터·콘텐츠 | 출처 표시 | 조건 |
|---|---|---|
| _(공공 데이터·이미지·폰트·음원을 쓰면 여기에 추가)_ | | 공공누리 유형 확인 |

앱 아이콘(임시)은 이 저장소에서 직접 만든 벡터다.
