# Add project specific ProGuard rules here.

# ---- NanoHTTPD ----
-keep class fi.iki.elonen.** { *; }
-dontwarn fi.iki.elonen.**

# ---- Kotlin Coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ---- Jetpack Compose ----
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---- App models & server ----
-keep class com.localide.model.** { *; }
-keep class com.localide.server.** { *; }
-keep class com.localide.auth.** { *; }

# ---- Credential Manager / Google Identity ----
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.**

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
# JSR 305 annotations
-dontwarn javax.annotation.**

# ---- AndroidX Browser (Custom Tabs) ----
-keep class androidx.browser.** { *; }
-dontwarn androidx.browser.**

# ---- DataStore ----
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---- JSON (org.json — built-in, no rules needed) ----

# ---- General Android rules ----
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepattributes Signature
-keepattributes Exceptions
