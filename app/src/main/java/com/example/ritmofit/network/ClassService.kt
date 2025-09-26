package com.example.ritmofit.network

import com.example.ritmofit.data.models.GymClass // Cambiado de model.Class a data.models.GymClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response

interface ClassService {
    @GET("api/classes")
    suspend fun getClasses(
        @Query("sede") sede: String? = null,
        @Query("discipline") discipline: String? = null,
        @Query("date") date: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<List<GymClass>> // Cambiado de Class a GymClass

    @GET("api/classes/{id}")
    suspend fun getClassDetail(
        @Path("id") classId: String
    ): Response<GymClass> // Cambiado de Class a GymClass
}
