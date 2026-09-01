package com.studioflow.backend.agendamento.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class AgendamentoRequest(
	@field:NotBlank(message = "profissionalId é obrigatório")
	val profissionalId: String,

	@field:NotBlank(message = "servicoId é obrigatório")
	val servicoId: String,

	@field:NotNull(message = "Data e hora são obrigatórias")
	@field:Future(message = "Data e hora devem estar no futuro")
	val dataHora: LocalDateTime
)
