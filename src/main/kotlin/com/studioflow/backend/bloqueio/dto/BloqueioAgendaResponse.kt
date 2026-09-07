package com.studioflow.backend.bloqueio.dto

import com.studioflow.backend.bloqueio.BloqueioAgenda
import java.time.LocalDateTime

data class BloqueioAgendaResponse(
	val id: String,
	val profissionalId: String,
	val inicio: LocalDateTime,
	val fim: LocalDateTime,
	val motivo: String
)

fun BloqueioAgenda.toResponse(): BloqueioAgendaResponse = BloqueioAgendaResponse(
	id = id!!,
	profissionalId = profissionalId,
	inicio = inicio,
	fim = fim,
	motivo = motivo
)
