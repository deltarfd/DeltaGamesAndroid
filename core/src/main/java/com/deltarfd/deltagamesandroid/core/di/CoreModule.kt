package com.deltarfd.deltagamesandroid.core.di

import androidx.room.Room
import com.deltarfd.deltagamesandroid.core.BuildConfig
import com.deltarfd.deltagamesandroid.core.data.local.GameDatabase
import com.deltarfd.deltagamesandroid.core.data.remote.ApiService
import com.deltarfd.deltagamesandroid.core.data.repository.GameRepositoryImpl
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
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
        Room.databaseBuilder(
            androidContext(),
            GameDatabase::class.java,
            "DeltaGames.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<GameDatabase>().gameDao() }
}

val repositoryModule = module {
    single<IGameRepository> { GameRepositoryImpl(get(), get()) }
}
