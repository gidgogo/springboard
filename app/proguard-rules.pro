# 현재 release 빌드는 isMinifyEnabled = false (코드 축소 안 함).
# 축소를 켜면 Pleos SDK 클래스가 지워지지 않도록 아래 규칙을 쓰고, 에뮬레이터에서 반드시 다시 확인한다.
#
# -keep class ai.pleos.playground.** { *; }
# -dontwarn ai.pleos.playground.**
#
# AAOS Car API 는 런타임에 차량이 제공한다.
# -dontwarn android.car.**
