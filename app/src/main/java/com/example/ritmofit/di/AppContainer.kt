// Archivo: AppContainer.kt
package com.example.ritmofit.di

import com.example.ritmofit.network.ApiService // Se cambiará si ApiService no es el tipo correcto
import com.example.ritmofit.network.ClassService // Asegurar que ClassService esté importado
import com.example.ritmofit.network.RetrofitClient

interface AppContainer {
    val classService: ClassService // Cambiado de apiService a classService y de ApiService a ClassService
}

class DefaultAppContainer : AppContainer {
    override val classService: ClassService by lazy { // Cambiado de apiService a classService y de ApiService a ClassService
        RetrofitClient.classService // Usar classService en lugar de apiService
    }
}
