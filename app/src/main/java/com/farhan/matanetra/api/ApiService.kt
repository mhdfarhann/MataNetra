package com.farhan.matanetra.api

import com.farhan.matanetra.response.RoutesResponse
import com.farhan.matanetra.response.ShortestPathRequest
import com.farhan.matanetra.response.ShortestPathResponse
import com.farhan.matanetra.response.SpeechToDestinationResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("speech-to-destination")
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Query("fetchData") fetchData: Boolean = true
    ): Call<SpeechToDestinationResponse>

    @POST("shortest-path")
    fun getShortestPath(@Body request: ShortestPathRequest): Call<ShortestPathResponse>

}