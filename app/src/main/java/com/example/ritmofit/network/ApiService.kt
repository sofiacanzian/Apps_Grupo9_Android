package com.example.ritmofit.network

import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.OtpResponse
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.data.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlinx.serialization.Serializable

interface ApiService {

    // Endpoints for authentication
    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: AuthRequest): Response<OtpResponse>

    @POST("api/users/verify-otp")
    suspend fun verifyOtp(@Body request: AuthRequest): Response<OtpResponse>

    // Endpoints for classes
    @GET("api/classes")
    suspend fun getClasses(): Response<List<GymClass>>

    @GET("api/classes/{classId}")
    suspend fun getClassDetails(@Path("classId") classId: String): Response<GymClass>

    // Endpoints for reservations
    @POST("api/reservations")
    suspend fun createReservation(@Body reservationRequest: ReservationRequest): Response<Reservation>

    @POST("api/reservations/{reservationId}/cancel")
    suspend fun cancelReservation(@Path("reservationId") reservationId: String): Response<Unit>
}

// Define la clase de datos ReservationRequest fuera de la interfaz
@Serializable
data class ReservationRequest(val userId: String, val gymClassId: String)