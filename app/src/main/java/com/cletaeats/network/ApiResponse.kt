package com.cletaeats.network

data class ApiResponse(
    val success: Boolean,
    val data: Any?, // Puede ser una lista de objetos o un objeto individual
    val error: String?
)