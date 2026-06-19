# Don't obfuscate (debuggable)
-dontobfuscate

# Keep Gson serialization classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mikamentos.app.data.model.** {
    *;
}
-keepclassmembers class com.mikamentos.app.data.model.** {
    <init>(...);
    *** component*();
    *** copy*(...);
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep entire app (prevent ProGuard from breaking features)
-keep class com.mikamentos.app.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
