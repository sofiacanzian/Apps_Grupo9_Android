//archivo: apiservice.kt
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

interface ApiService {

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body authRequest: AuthRequest): Response<Void>

    // Ruta de confirmación de OTP modificada para esperar un objeto UserResponse
    @POST("api/auth/confirm-otp")
    suspend fun confirmOtp(@Body otpRequest: OtpConfirmationRequest): Response<UserResponse>

    @GET("api/classes")
    suspend fun getClasses(): Response<List<GymClass>>

    @GET("api/reservations/{userId}")
    suspend fun getUserReservations(@Path("userId") userId: String): Response<List<Reservation>>

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