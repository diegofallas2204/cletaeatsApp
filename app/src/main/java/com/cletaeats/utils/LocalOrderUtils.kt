package com.cletaeats.utils

object LocalOrderUtils {

    // IDs negativos garantizan cero colisión con IDs del servidor (siempre positivos)
    const val LOCAL_ID_MIN = -9999
    const val LOCAL_ID_MAX = -1000

    fun isLocalOnlyOrderId(id: Int): Boolean = id < 0

    fun generateLocalOrderId(): Int = (LOCAL_ID_MIN..LOCAL_ID_MAX).random()
}
