package com.deltarfd.deltagamesandroid.core.di

import androidx.room.Room
import com.deltarfd.deltagamesandroid.core.BuildConfig
import com.deltarfd.deltagamesandroid.core.data.local.GameDatabase
import com.deltarfd.deltagamesandroid.core.data.remote.ApiService
import com.deltarfd.deltagamesandroid.core.data.repository.GameRepositoryImpl
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import com.deltarfd.deltagamesandroid.core.utils.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val API_HOST = "api.rawg.io"

val networkModule = module {
    single {
        val certificatePinner = CertificatePinner.Builder()
            .add(API_HOST, "sha256/klEldM61oXP+Ol1yIV5G4odrqhNnbHJbu50iV+pupVA=")
            .add(API_HOST, "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=")
            .add(API_HOST, "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=")
            .build()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
    }
    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }
}

val databaseModule = module {
    single {
        // Database encryption using SQLCipher with a randomly generated passphrase
        // stored securely in Android Keystore (via EncryptedSharedPreferences)
        val passphrase = DatabasePassphraseProvider.getPassphrase(androidContext())
        val factory = SupportOpenHelperFactory(passphrase)

        Room.databaseBuilder(
            androidContext(),
            GameDatabase::class.java,
            "DeltaGames_encrypted.db"
        )
            .fallbackToDestructiveMigration(false)
            .openHelperFactory(factory)
            .build()
    }
    single { get<GameDatabase>().gameDao() }
}

val repositoryModule = module {
    single<IGameRepository> { GameRepositoryImpl(get(), get()) }
}
