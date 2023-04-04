# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-Error inflating class androidx.fragment.app.FragmentContainerView 에러 해결용
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}

#-Error inflating class androidx.fragment.app.FragmentContainerView 에러 해결용 2
-keepnames class * implements android.os.Parcelable
-keepnames class com.dreamreco.firebaseappstreamtest.room.entity.DiaryBase

# Caused by: java.lang.ClassNotFoundException: com.google.android.play.core.review 에러 해결용
-keep class com.google.android.play.core.** { *; }

# 검색 서치뷰 사용 시 에러 해결용
-keep class androidx.appcompat.widget.SearchView { *; }


# fireBaase SDK Room Null error 해결용
-keepclassmembers class com.dreamreco.firebaseappstreamtest.** { <fields>; }
#-keepclassmembers class com.dreamreco.** { <fields>; }

## FireStore 연결 dataclass 에러 : java.lang.RuntimeException: No properties to serialize found on class 해결용
#-keepclassmembers class com.dreamreco.firebaseappstreamtest.ui.firestorelist.Wine.** { *; }
#-keepclassmembers class com.dreamreco.firebaseappstreamtest.ui.firestorelist.NewWine.** { *; }
#
#-keep class com.dreamreco.firebaseappstreamtest.ui.firestorelist.Wine.** { *; }
#-keep class com.dreamreco.firebaseappstreamtest.ui.firestorelist.NewWine.** { *; }


# 카카오 API 적용
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**