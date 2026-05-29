package com.cletaeats.database

import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

object SyncErrorUtils {

    fun isTransientFailure(e: Exception): Boolean {
        if (e is UnknownHostException) return true
        if (e is IOException) {
            val msg = e.message?.lowercase() ?: ""
            if (msg.contains("unable to resolve host") || msg.contains("failed to connect")) {
                return true
            }
        }
        if (e is HttpException) {
            val code = e.code()
            if (code in 500..599) return true
            val body = readHttpErrorBody(e).lowercase()
            if (body.contains("base de datos") || body.contains("database") || body.contains("connection")) {
                return true
            }
        }
        return false
    }

    fun readHttpErrorBody(e: HttpException): String {
        return try {
            e.response()?.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            e.message().orEmpty()
        }
    }

    fun describeFailure(e: Exception): String {
        return if (e is HttpException) {
            val body = readHttpErrorBody(e)
            if (body.isNotBlank()) "HTTP ${e.code()} $body" else "HTTP ${e.code()}"
        } else {
            e.message ?: e.javaClass.simpleName
        }
    }
}
