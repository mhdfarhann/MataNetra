package com.farhan.matanetra.response

data class ShortestPathRequest(
    val source: String,
    val target: String
)
data class ShortestPathResponse(
    val path: List<Destination>
)
