# ── Core module ProGuard rules ────────────────────────────────────────────────

# Keep classes accessed via reflection by Koin DI
-keep class com.deltarfd.deltagamesandroid.core.di.** { *; }

# Keep Room database & DAO (instantiated via reflection)
-keep class com.deltarfd.deltagamesandroid.core.data.local.GameDatabase { *; }
-keep class com.deltarfd.deltagamesandroid.core.data.local.GameDao { *; }

# Keep repository (injected by Koin via interface)
-keep class com.deltarfd.deltagamesandroid.core.data.repository.GameRepositoryImpl { *; }

# Keep domain interfaces/classes (referenced by app & favorite modules)
-keep class com.deltarfd.deltagamesandroid.core.domain.** { *; }

# Keep utility classes (used directly by other modules)
-keep class com.deltarfd.deltagamesandroid.core.utils.DatabasePassphraseProvider { *; }
-keep class com.deltarfd.deltagamesandroid.core.utils.DataMapper { *; }
-keep class com.deltarfd.deltagamesandroid.core.utils.Resource { *; }
-keep class com.deltarfd.deltagamesandroid.core.utils.Resource$* { *; }

# Keep data models (Gson serialization + Room entities)
-keep class com.deltarfd.deltagamesandroid.core.data.remote.response.** { *; }
-keep class com.deltarfd.deltagamesandroid.core.data.local.entity.** { *; }

# ── SQLCipher ─────────────────────────────────────────────────────────────────
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class com.deltarfd.deltagamesandroid.core.data.remote.ApiService { *; }

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Gson ──────────────────────────────────────────────────────────────────────
-keepattributes Signature
-keep class com.google.gson.stream.** { *; }
