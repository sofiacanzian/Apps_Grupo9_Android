// Archivo: RitmoFitApplication.kt
package com.example.ritmofit

import android.app.Application
import com.example.ritmofit.di.AppContainer
import com.example.ritmofit.di.DefaultAppContainer
import com.jakewharton.threetenabp.AndroidThreeTen // Importación necesaria

class RitmoFitApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar ThreeTenABP para habilitar java.time en APIs antiguas
        AndroidThreeTen.init(this)

        // 2. Inicialización de tu contenedor de dependencias
        container = DefaultAppContainer()
    }
}