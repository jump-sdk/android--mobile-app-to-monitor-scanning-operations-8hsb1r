# This file specifies which parts of the code should be preserved during optimization

# Add any project specific keep options here

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Android common rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keep public class * extends java.lang.Exception

# Kotlin Serialization rules
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { 
    *** Companion; 
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class com.jump.scanmonitor.model.**$$serializer { *; }
-keepclassmembers class com.jump.scanmonitor.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit rules
-keep,allowobfuscation interface * { @retrofit2.http.* <methods>; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class com.jump.scanmonitor.service.api.DatadogApiService { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Koin rules
-keep class org.koin.** { *; }
-keep class com.jump.scanmonitor.di.AppModule { *; }
-keep class * { public <init>(...); }

# Model class rules
-keep class com.jump.scanmonitor.model.** { *; }
-keep class com.jump.scanmonitor.model.ApiResponse { *; }
-keep class com.jump.scanmonitor.model.Series { *; }
-keep class com.jump.scanmonitor.model.Point { *; }
-keep class com.jump.scanmonitor.model.Metadata { *; }
-keep class com.jump.scanmonitor.model.ScanMetrics { *; }
-keep class com.jump.scanmonitor.model.UiState { *; }
-keep class com.jump.scanmonitor.model.NetworkStatus { *; }
-keep class com.jump.scanmonitor.model.Result { *; }

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }
-keep class androidx.lifecycle.viewmodel.compose.** { *; }

# Firebase rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**