// Archivo: RetrofitClient.kt (Corregido con kotlinx.serialization)
package com.example.ritmofit.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Crea un interceptor para ver los logs de HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configura el cliente de OkHttp para usar el interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Crea un objeto JSON para el serializador
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // Usa el convertidor de kotlinx.serialization
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}