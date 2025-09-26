package com.example.ritmofit.data.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.GlobalScope // 🚨 IMPORTANTE: Usamos GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // Se puede usar para inicializar

// 1. Extensión para obtener la instancia única de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

object SessionManager {
    private lateinit var dataStore: DataStore<Preferences>

    // Keys para DataStore
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    // Flujo que publica si el token existe
    val isLoggedIn: StateFlow<Boolean>
        get() = _isLoggedIn

    // Flujo interno para la observabilidad
    private lateinit var _isLoggedIn: StateFlow<Boolean>

    /**
     * Inicializa el SessionManager y carga los datos guardados en DataStore.
     * DEBE LLAMARSE UNA VEZ en la clase Application.
     */
    fun initialize(context: Context) {
        if (!::dataStore.isInitialized) {
            dataStore = context.dataStore

            // Mapea el flujo de DataStore para crear el StateFlow observable
            _isLoggedIn = dataStore.data
                .map { preferences ->
                    // El valor es true si el token existe, false si es null
                    preferences[TOKEN_KEY] != null
                }
                .stateIn(
                    // 🚨 CORRECCIÓN CLAVE: Usamos GlobalScope.
                    // Esto le da al StateFlow un ámbito de larga duración (vida de la app)
                    // sin usar runBlocking, que puede causar bloqueos.
                    scope = GlobalScope,
                    started = SharingStarted.Eagerly,
                    initialValue = false
                )
        }
    }

    /**
     * Guarda el token y el ID del usuario, y actualiza el flujo isLoggedIn.
     * @param token El JWT recibido del servidor.
     * @param userId El ID del usuario.
     */
    suspend fun setSession(token: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Cierra la sesión, eliminando el token y el ID de DataStore.
     */
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Obtiene el token de autenticación para el Interceptor OkHttp.
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }

    /**
     * Obtiene el ID del usuario.
     */
    suspend fun getUserId(): String? {
        return dataStore.data.first()[USER_ID_KEY]
    }
}