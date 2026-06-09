package com.cletaeats.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SessionEvent {
    /** El servidor rechazó la sesión porque el admin deshabilitó la cuenta. */
    object AccountDisabled : SessionEvent()

    /** El usuario autenticado no tiene perfil de repartidor en la BD. */
    object ProfileNotFound : SessionEvent()
}

/**
 * Canal singleton para emitir eventos de sesión desde cualquier capa
 * (interceptor HTTP, repositorio, etc.) y reaccionar en la UI global.
 *
 * Uso en interceptor:  SessionEvents.emit(SessionEvent.AccountDisabled)
 * Uso en MainActivity: SessionEvents.events.collect { ... }
 */
object SessionEvents {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emit(event: SessionEvent) {
        _events.tryEmit(event)
    }
}
