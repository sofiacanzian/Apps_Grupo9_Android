// archivo: apiservice.kt (FINAL CORREGIDO)
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
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// NOTA: Debes definir la interfaz o clase FilterResponse en tu código
// interface FilterResponse { ... }

interface ApiService {

    // ----------------------------------------------------
    // 🚀 ENDPOINTS DE AUTENTICACIÓN (RUTAS CORREGIDAS PARA EL 404)
    // ----------------------------------------------------
    // Se ha REINSTALADO el prefijo "api/" para que el servidor las encuentre.

    @POST("api/auth/register-send-otp") // 1. Registro: Enviar datos y solicitar OTP
    suspend fun registerAndSendOtp(@Body request: RegistrationRequest): Response<Unit>

    @POST("api/auth/login-send-otp") // 2. Login: Enviar credenciales y solicitar OTP
    suspend fun loginAndSendOtp(@Body authRequest: AuthRequest): Response<Unit>

    @POST("api/auth/verify-otp-and-login") // 3. Verificar OTP (usado para registro/login/recuperación)
    suspend fun verifyOtpAndLogin(@Body otpRequest: OtpConfirmationRequest): Response<UserResponse>

    @POST("api/auth/request-password-reset") // 4. Solicitar OTP de Recuperación
    suspend fun requestPasswordResetOtp(@Body authRequest: AuthRequest): Response<Unit>

    @POST("api/auth/reset-password") // 5. Restablecer contraseña con OTP
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<Unit>


    // ----------------------------------------------------
    // OTROS ENDPOINTS (Mantenidos)
    // ----------------------------------------------------

    @GET("api/classes")
    suspend fun getClasses(
        @Query("location") location: String? = null,
        @Query("discipline") discipline: String? = null,
        @Query("date") date: String? = null
    ): Response<List<GymClass>>

    @GET("api/filters")
    suspend fun getFilters(): Response<FilterResponse>

    @GET("api/reservations/{userId}")
    suspend fun getReservations(@Path("userId") userId: String): Response<List<Reservation>>

    @GET("api/history/{userId}")
    suspend fun getAttendanceHistory(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<Reservation>>

    @POST("api/reservations")
    suspend fun createReservation(
        @Body reservationData: Map<String, String>
    ): Response<Reservation>

    @POST("api/reservations/cancel/{reservationId}")
    suspend fun cancelReservation(@Path("reservationId") reservationId: String): Response<Unit>

    @GET("api/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>

    @PUT("api/profile/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<User>

    @GET("api/classes/{classId}")
    suspend fun getClassDetails(@Path("classId") classId: String): Response<GymClass>
}