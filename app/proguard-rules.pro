# Add project specific ProGuard rules here.

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retrofit
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

# Gson
-dontwarn sun.misc.**

# Glide
-dontwarn com.bumptech.glide.**

# SQLCipher
-keep class net.zetetic.database.** { *; }

# Room
-dontwarn androidx.room.paging.**

# Keep data models (needed for Gson serialization)
-keep class com.deltarfd.deltagamesandroid.core.data.remote.response.** { *; }
-keep class com.deltarfd.deltagamesandroid.core.data.local.entity.** { *; }
-keep class com.deltarfd.deltagamesandroid.core.domain.model.** { *; }

# Keep Kotlin stdlib classes required by dynamic feature modules
-keep,allowobfuscation class kotlin.** { *; }
-keep,allowobfuscation class kotlinx.** { *; }
