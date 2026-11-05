pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pleos Connect SDK 공식 Maven 저장소 (토큰 불필요).
        // 공식 샘플: https://github.com/Pleos-Playground/pleos-sample-vehicle-app
        // ai.pleos.* 그룹만 이 저장소에서 받도록 제한한다 (의존성 혼동 공격 방지).
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/") {
            content { includeGroupByRegex("ai\\.pleos\\..*") }
        }
    }
}

rootProject.name = "springboard"

// :core = 안드로이드에 의존하지 않는 순수 Kotlin 로직 (에뮬레이터 없이 단위 테스트)
// :app  = Pleos Connect 안드로이드 앱 (SDK 연동, 화면)
include(":core")
include(":app")
