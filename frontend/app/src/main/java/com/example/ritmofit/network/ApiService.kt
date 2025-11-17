package com.example.ritmofit.network

import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.OtpConfirmationRequest
import com.example.ritmofit.data.models.PasswordResetRequest
import com.example.ritmofit.data.models.RegistrationRequest
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.data.models.User
import com.example.ritmofit.data.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// NOTA: Debes definir la interfaz o clase FilterResponse en tu código
// interface FilterResponse { ... }

interface ApiService {

    // ----------------------------------------------------
    // 🚀 ENDPOINTS DE AUTENTICACIÓN
    // ----------------------------------------------------

    @POST("api/auth/register-send-otp") // 1. Registro: Enviar datos y solicitar OTP
    suspend fun registerAndSendOtp(@Body request: RegistrationRequest): Response<Unit>

    // ✅ CAMBIO: Login directo sin OTP. Usa la nueva ruta /api/auth/login.
    @POST("api/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<UserResponse>

    // ❌ ELIMINADA: La antigua función verifyOtpAndLogin.

    @POST("api/auth/request-password-reset") // 4. Solicitar OTP de Recuperación
    suspend fun requestPasswordResetOtp(@Body authRequest: AuthRequest): Response<Unit>

    // 🔑 MANTENER: Verifica que el OTP de recuperación/registro sea válido sin iniciar sesión
    @POST("api/auth/verify-reset-otp")
    suspend fun verifyResetOtp(@Body otpRequest: OtpConfirmationRequest): Response<Unit>

    @POST("api/auth/reset-password") // 5. Restablecer contraseña con OTP
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<Unit>


    // ----------------------------------------------------
    // 👤 ENDPOINTS DE PERFIL (PROTEGIDOS)
    // ----------------------------------------------------

    @GET("api/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>

    @PUT("api/profile/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<User>

    // ----------------------------------------------------
    // 🏋️ ENDPOINTS DE CLASES (PROTEGIDOS - CRUD USUARIO/ADMIN)
    // ----------------------------------------------------

    @GET("api/classes")
    suspend fun getClasses(
        @Query("location") location: String? = null,
        @Query("discipline") discipline: String? = null,
        @Query("date") date: String? = null
    ): Response<List<GymClass>>

    @GET("api/filters")
    suspend fun getFilters(): Response<FilterResponse>

    @GET("api/classes/{classId}")
    suspend fun getClassDetails(@Path("classId") classId: String): Response<GymClass>

    // Rutas de administración de clases (Asumiendo que requieren token de Admin)
    @POST("api/classes")
    suspend fun createClass(@Body gymClass: GymClass): Response<GymClass>

    @PUT("api/classes/{classId}")
    suspend fun updateClass(
        @Path("classId") classId: String,
        @Body gymClass: GymClass
    ): Response<GymClass>

    @DELETE("api/classes/{classId}")
    suspend fun deleteClass(@Path("classId") classId: String): Response<Unit>


    // ----------------------------------------------------
    // 📅 ENDPOINTS DE RESERVAS E HISTORIAL (PROTEGIDOS)
    // ----------------------------------------------------

    @POST("api/reservations")
    suspend fun createReservation(
        @Body reservationData: Map<String, String>
    ): Response<Reservation>

    @GET("api/reservations/{userId}")
    suspend fun getReservations(@Path("userId") userId: String): Response<List<Reservation>>

    @POST("api/reservations/cancel/{reservationId}")
    suspend fun cancelReservation(@Path("reservationId") reservationId: String): Response<Unit>

    @GET("api/history/{userId}")
    suspend fun getAttendanceHistory(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<Reservation>>
}