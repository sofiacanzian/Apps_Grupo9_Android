// archivo: apiservice.kt
package com.example.ritmofit.network

import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.OtpConfirmationRequest
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

// --- SE ELIMINA LA DEFINICIÓN DE INTERFACE REDUNDANTE AQUÍ ---
// import kotlinx.serialization.Serializable
// interface FilterResponse {
//     val locations: List<String>
//     val disciplines: List<String>
// }
// ---------------------------------------------------------------------------------------


interface ApiService {

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body authRequest: AuthRequest): Response<Void>

    @POST("api/auth/confirm-otp")
    suspend fun confirmOtp(@Body otpRequest: OtpConfirmationRequest): Response<UserResponse>

    @GET("api/classes")
    suspend fun getClasses(
        @Query("location") location: String? = null,
        @Query("discipline") discipline: String? = null,
        @Query("date") date: String? = null
    ): Response<List<GymClass>>

    @GET("api/filters")
    // Se usará la clase FilterResponse definida en el archivo FilterResponse.kt
    suspend fun getFilters(): Response<FilterResponse>

    // Ruta para reservas activas/futuras
    @GET("api/reservations/{userId}")
    suspend fun getReservations(@Path("userId") userId: String): Response<List<Reservation>>

    // --- RUTA PARA HISTORIAL CON FILTROS ---
    @GET("api/history/{userId}")
    suspend fun getAttendanceHistory(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<Reservation>>
    // ------------------------------------

    @POST("api/reservations")
    suspend fun createReservation(
        @Body reservationData: Map<String, String>
    ): Response<Reservation>

    @POST("api/reservations/cancel/{reservationId}")
    suspend fun cancelReservation(@Path("reservationId") reservationId: String): Response<Void>

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