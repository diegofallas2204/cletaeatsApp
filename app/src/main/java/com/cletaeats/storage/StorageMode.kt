package com.cletaeats.storage

enum class StorageMode {
    API,             // Internet + API respondiendo normalmente
    LOCAL,           // Sin internet (ilimitado) o API caída + local disponible
    CLOUD_OVERFLOW,  // API caída + local lleno (contador >= umbral)
    CLOUD_FORCED     // Toggle manual del usuario (requiere internet)
}

fun StorageMode.label(): String = when (this) {
    StorageMode.API            -> "Conectado al servidor"
    StorageMode.LOCAL          -> "Almacenamiento local"
    StorageMode.CLOUD_OVERFLOW -> "Memoria llena · Usando nube"
    StorageMode.CLOUD_FORCED   -> "Modo nube activado"
}

fun StorageMode.isCloud(): Boolean =
    this == StorageMode.CLOUD_OVERFLOW || this == StorageMode.CLOUD_FORCED
