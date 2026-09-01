package com.studioflow.backend.pagamento.dto

import com.studioflow.backend.pagamento.MetodoPagamento
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ConfirmarPagamentoRequest(
	@field:NotBlank(message = "agendamentoId é obrigatório")
	val agendamentoId: String,

	@field:NotNull(message = "Método de pagamento é obrigatório")
	val metodo: MetodoPagamento
)
