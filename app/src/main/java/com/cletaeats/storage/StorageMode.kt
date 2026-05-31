package com.cletaeats.storage

enum class StorageMode {
    API,            // Internet + API respondiendo (default)
    LOCAL,          // Sin internet — local ilimitado
    DISK_EXPANSION, // Modo expansión: local limitado (antes del tope)
    CLOUD_OVERFLOW, // Expansión llena → desborda a nube
    CLOUD_FORCED    // Nube forzada manualmente
}

fun StorageMode.label(): String = when (this) {
    StorageMode.API            -> "Conectado al servidor"
    StorageMode.LOCAL          -> "Sin conexión · Local ilimitado"
    StorageMode.DISK_EXPANSION -> "Expansión de disco activa"
    StorageMode.CLOUD_OVERFLOW -> "Disco lleno · Usando nube"
    StorageMode.CLOUD_FORCED   -> "Nube forzada"
}

fun StorageMode.isCloud(): Boolean =
    this == StorageMode.CLOUD_OVERFLOW || this == StorageMode.CLOUD_FORCED
