# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-repackageclasses
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Kotlin stdlib needed by dynamic feature module (loaded via separate split APK)
-keep class kotlin.LazyKt { *; }
-keep class kotlin.LazyKt__LazyJVMKt { *; }
-keep class kotlin.jvm.internal.Intrinsics { *; }
-keep class kotlin.jvm.internal.Ref { *; }
-keep class kotlin.jvm.internal.Ref$ObjectRef { *; }
-keep class kotlin.jvm.internal.Ref$BooleanRef { *; }
-keep class kotlin.jvm.internal.Ref$IntRef { *; }
-keep class kotlin.jvm.internal.DefaultConstructorMarker { *; }
-keep class kotlin.jvm.internal.Lambda { *; }
-keep class kotlin.jvm.internal.FunctionBase { *; }

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Gson serialization (keep field names) ─────────────────────────────────────
-dontwarn sun.misc.**
-keep class com.deltarfd.deltagamesandroid.core.data.remote.response.** { *; }
-keep class com.deltarfd.deltagamesandroid.core.data.local.entity.** { *; }
-keep class com.deltarfd.deltagamesandroid.core.domain.model.** { *; }

# ── Glide ─────────────────────────────────────────────────────────────────────
-dontwarn com.bumptech.glide.**

# ── SQLCipher ─────────────────────────────────────────────────────────────────
-keep class net.zetetic.database.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-dontwarn androidx.room.paging.**

# ── Koin (reflection-based DI) ────────────────────────────────────────────────
-keep class com.deltarfd.deltagamesandroid.core.di.** { *; }
-keep class com.deltarfd.deltagamesandroid.di.** { *; }
