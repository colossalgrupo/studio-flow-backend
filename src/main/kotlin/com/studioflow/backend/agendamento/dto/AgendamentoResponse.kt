package com.studioflow.backend.agendamento.dto

import com.studioflow.backend.agendamento.Agendamento
import com.studioflow.backend.agendamento.StatusAgendamento
import java.time.LocalDateTime

data class AgendamentoResponse(
	val id: String,
	val clienteId: String,
	val profissionalId: String,
	val servicoId: String,
	val dataHora: LocalDateTime,
	val status: StatusAgendamento
)

fun Agendamento.toResponse(): AgendamentoResponse = AgendamentoResponse(
	id = id!!,
	clienteId = clienteId,
	profissionalId = profissionalId,
	servicoId = servicoId,
	dataHora = dataHora,
	status = status
)
