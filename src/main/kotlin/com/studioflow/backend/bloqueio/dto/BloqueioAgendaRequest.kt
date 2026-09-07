package com.studioflow.backend.bloqueio.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class BloqueioAgendaRequest(
	@field:NotBlank(message = "profissionalId é obrigatório")
	val profissionalId: String,

	@field:NotNull(message = "Início é obrigatório")
	val inicio: LocalDateTime,

	@field:NotNull(message = "Fim é obrigatório")
	val fim: LocalDateTime,

	@field:NotBlank(message = "Motivo é obrigatório")
	val motivo: String
)
