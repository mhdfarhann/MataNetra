package com.farhan.matanetra.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Config {
    companion object{
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://103.127.97.215:5000")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
    }

}