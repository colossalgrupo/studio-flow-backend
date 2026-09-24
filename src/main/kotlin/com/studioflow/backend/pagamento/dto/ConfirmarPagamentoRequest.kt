package com.studioflow.backend.pagamento.dto

import com.studioflow.backend.pagamento.MetodoPagamento
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ConfirmarPagamentoRequest(
	@field:NotBlank(message = "agendamentoId é obrigatório")
	val agendamentoId: String,

	@field:NotNull(message = "Método de pagamento é obrigatório")
	val metodo: MetodoPagamento,

	/** Obrigatório só quando o gateway de pagamento real (Asaas) está ativo — necessário pra
	 * criar/identificar o cliente pagador na Asaas. Ignorado no gateway mock. */
	val cpf: String? = null
)
