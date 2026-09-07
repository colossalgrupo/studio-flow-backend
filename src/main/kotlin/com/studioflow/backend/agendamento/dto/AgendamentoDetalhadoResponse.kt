package com.studioflow.backend.agendamento.dto

import com.studioflow.backend.agendamento.StatusAgendamento
import java.math.BigDecimal
import java.time.LocalDateTime

data class AgendamentoDetalhadoResponse(
	val id: String,
	val clienteId: String,
	val clienteNome: String,
	val profissionalId: String,
	val profissionalNome: String,
	val servicoId: String,
	val servicoNome: String,
	val inicio: LocalDateTime,
	val fim: LocalDateTime,
	val valor: BigDecimal,
	val status: StatusAgendamento
)
