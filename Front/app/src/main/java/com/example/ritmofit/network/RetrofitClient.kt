package com.example.ritmofit.network

import com.example.ritmofit.data.models.SessionManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // 1. Crea un interceptor para ver los logs de HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. CRÍTICO: Interceptor para añadir el token JWT a las peticiones
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()

        // El DataStore es asíncrono, pero OkHttp requiere síncrono, usamos runBlocking.
        val token = runBlocking {
            SessionManager.getAuthToken()
        }

        val requestBuilder = originalRequest.newBuilder()

        if (token != null) {
            // Añadir el token a la cabecera "Authorization"
            requestBuilder.header("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    // 3. Configura el cliente de OkHttp para usar AMBOS interceptores
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    // Crea un objeto JSON para el serializador
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}