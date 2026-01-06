package com.example.runningtracker

import android.app.Application
import com.example.runningtracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RunningTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RunningTrackerApp)
            modules(appModule)
        }
    }
}
