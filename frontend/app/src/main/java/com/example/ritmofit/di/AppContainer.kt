// Archivo: AppContainer.kt
package com.example.ritmofit.di

import com.example.ritmofit.network.ApiService
import com.example.ritmofit.network.RetrofitClient

interface AppContainer {
    val apiService: ApiService
}

class DefaultAppContainer : AppContainer {
    override val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }
}