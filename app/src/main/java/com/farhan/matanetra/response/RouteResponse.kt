package com.farhan.matanetra.response

import com.google.gson.annotations.SerializedName

data class RoutesResponse(
    @SerializedName("routes")
    val routes: List<Route>
)

data class Route(
    @SerializedName("deskripsi")
    val description: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("lat")
    val latitude: Double,

    @SerializedName("long")
    val longitude: Double,

    @SerializedName("title")
    val title: String
)