package com.cletaeats.network

// Estructura de respuesta unificada obligatoria


// Modelo del Combo basado en la DB (MySQL)
data class Combo(
    val numero_combo: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String?
)