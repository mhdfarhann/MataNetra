package com.farhan.matanetra.response

data class SpeechToDestinationResponse(
    val destination: Destination,
    val status: String
)

data class Destination(
    val id: String,
    val title: String,
    val deskripsi: String,
    val lat: Double,
    val long: Double
)

