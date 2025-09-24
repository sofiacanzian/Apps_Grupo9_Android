package com.example.ritmofit.network

import com.example.ritmofit.model.Class
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
    ): Response<List<Class>>

    @GET("api/classes/{id}")
    suspend fun getClassDetail(
        @Path("id") classId: String
    ): Response<Class>
}
