package com.cletaeats.utils

object LocalOrderUtils {

    const val LOCAL_ID_MIN = 1000
    const val LOCAL_ID_MAX = 9999

    fun isLocalOnlyOrderId(id: Int): Boolean = id in LOCAL_ID_MIN..LOCAL_ID_MAX

    fun generateLocalOrderId(): Int = (LOCAL_ID_MIN..LOCAL_ID_MAX).random()
}
