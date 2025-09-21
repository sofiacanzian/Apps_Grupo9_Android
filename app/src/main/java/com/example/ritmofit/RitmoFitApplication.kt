// Archivo: RitmoFitApplication.kt
package com.example.ritmofit

import android.app.Application
import com.example.ritmofit.di.AppContainer
import com.example.ritmofit.di.DefaultAppContainer

class RitmoFitApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}