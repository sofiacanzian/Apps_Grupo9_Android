// Archivo: RitmoFitApplication.kt (MODIFICADO)
package com.example.ritmofit

import android.app.Application
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.di.AppContainer
import com.example.ritmofit.di.DefaultAppContainer
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch // Importa esto

class RitmoFitApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar ThreeTenABP
        AndroidThreeTen.init(this)

        // 2. Inicialización del contenedor de dependencias
        container = DefaultAppContainer()

        // 3. CRÍTICO: Inicializar el SessionManager
        SessionManager.initialize(applicationContext)

        // 🚨 SOLUCIÓN TEMPORAL PARA DESARROLLO: Forzar el cierre de sesión.
        // Esto asegura que siempre empieces en la pantalla de Login.
        GlobalScope.launch {
            SessionManager.logout()
        }
    }
}