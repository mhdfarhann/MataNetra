package com.farhan.matanetra.api

import com.farhan.matanetra.response.RoutesResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/get-routes")
    fun getRoutes(): Call<RoutesResponse>
}