package com.example.ritmofit.data.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers // ✅ Agregamos Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking // Usaremos runBlocking para la inicialización segura

// 1. Extensión para obtener la instancia única de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

// ✅ MODIFICACIÓN: Definimos un ámbito de aplicación
private val ApplicationScope = CoroutineScope(Dispatchers.IO)

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

    // ✅ MODIFICACIÓN: Flujo para exponer el User ID
    val currentUserId: StateFlow<String?>
        get() = _currentUserId

    private lateinit var _currentUserId: StateFlow<String?>

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
                    // ✅ MODIFICACIÓN: Usamos el CoroutineScope definido.
                    scope = ApplicationScope,
                    started = SharingStarted.Eagerly,
                    initialValue = runBlocking { dataStore.data.first()[TOKEN_KEY] != null } // Inicialización más segura
                )

            // ✅ NUEVO: Inicialización del StateFlow del User ID
            _currentUserId = dataStore.data
                .map { preferences ->
                    preferences[USER_ID_KEY]
                }
                .stateIn(
                    scope = ApplicationScope,
                    started = SharingStarted.Eagerly,
                    initialValue = runBlocking { dataStore.data.first()[USER_ID_KEY] }
                )
        }
    }

    /**
     * Guarda el token y el ID del usuario, y actualiza los flujos.
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
     * NOTA: Este NO usa StateFlow, es una lectura única para el Interceptor.
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }

    /**
     * Obtiene el ID del usuario de forma síncrona/reactiva (útil en Composable/ViewModel).
     * ✅ MODIFICACIÓN: Usa el StateFlow observable.
     */
    fun getUserId(): String? {
        // En código fuera de corrutinas o Composable, puedes usar el valor actual del StateFlow.
        return _currentUserId.value
    }

    /**
     * Obtiene el ID del usuario de forma asíncrona (si se necesita esperar).
     */
    suspend fun getUserIdAsync(): String? {
        return dataStore.data.first()[USER_ID_KEY]
    }
}