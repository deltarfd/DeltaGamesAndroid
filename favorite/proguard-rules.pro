# ── Favorite dynamic feature module ───────────────────────────────────────────

# Keep fragment (instantiated by Navigation via reflection)
-keep class com.deltarfd.deltagamesandroid.favorite.ui.FavoriteFragment { *; }
-keep class com.deltarfd.deltagamesandroid.favorite.ui.FavoriteViewModel { *; }
-keep class com.deltarfd.deltagamesandroid.favorite.ui.FavoriteAdapter { *; }

# Keep Koin module (loaded dynamically)
-keep class com.deltarfd.deltagamesandroid.favorite.di.FavoriteModuleKt { *; }
