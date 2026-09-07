package com.studioflow.backend.agendamento.dto

import com.studioflow.backend.agendamento.StatusAgendamento
import jakarta.validation.constraints.NotNull

data class AtualizarStatusAgendamentoRequest(
	@field:NotNull(message = "Status é obrigatório")
	val status: StatusAgendamento
)
