package com.example.ritmofit

import android.app.Application
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.di.AppContainer
import com.example.ritmofit.di.DefaultAppContainer
import com.jakewharton.threetenabp.AndroidThreeTen

class RitmoFitApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        container = DefaultAppContainer()
        SessionManager.initialize(applicationContext)
    }
}
