package com.deltarfd.deltagamesandroid.core.di

import android.content.Context
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
private const val DB_NAME = "DeltaGames_encrypted.db"

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
    single { buildGameDatabase(androidContext()) }
    single { get<GameDatabase>().gameDao() }
}

val repositoryModule = module {
    single<IGameRepository> { GameRepositoryImpl(get(), get()) }
}

private fun buildGameDatabase(context: Context): GameDatabase {
    val passphrase = DatabasePassphraseProvider.getPassphrase(context)
    val factory = SupportOpenHelperFactory(passphrase)

    return Room.databaseBuilder(context, GameDatabase::class.java, DB_NAME)
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration(false)
        .build()
}
