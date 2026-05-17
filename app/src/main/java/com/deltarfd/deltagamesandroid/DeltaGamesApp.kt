package com.deltarfd.deltagamesandroid

import android.app.Application
import com.deltarfd.deltagamesandroid.core.di.databaseModule
import com.deltarfd.deltagamesandroid.core.di.networkModule
import com.deltarfd.deltagamesandroid.core.di.repositoryModule
import com.deltarfd.deltagamesandroid.di.useCaseModule
import com.deltarfd.deltagamesandroid.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DeltaGamesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DeltaGamesApp)
            modules(listOf(networkModule, databaseModule, repositoryModule, useCaseModule, viewModelModule))
        }
    }
}
