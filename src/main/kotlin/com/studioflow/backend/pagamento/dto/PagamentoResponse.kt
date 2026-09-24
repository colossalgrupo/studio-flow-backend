package com.studioflow.backend.pagamento.dto

import com.studioflow.backend.pagamento.MetodoPagamento
import com.studioflow.backend.pagamento.Pagamento
import com.studioflow.backend.pagamento.StatusPagamento
import java.math.BigDecimal

data class PagamentoResponse(
	val id: String,
	val agendamentoId: String,
	val valorTotal: BigDecimal,
	val metodo: MetodoPagamento,
	val statusPsp: String,
	val status: StatusPagamento,
	val split: SplitPagamentoResponse?,
	// Preenchidos só na resposta de criação de uma cobrança Pix pendente — o cliente usa
	// isso pra pagar. Não fica guardado no banco (é reobtido via Asaas se precisar de novo).
	val qrCodePayload: String? = null,
	val qrCodeImagemBase64: String? = null
)

fun Pagamento.toResponse(
	split: SplitPagamentoResponse?,
	qrCodePayload: String? = null,
	qrCodeImagemBase64: String? = null
): PagamentoResponse = PagamentoResponse(
	id = id!!,
	agendamentoId = agendamentoId,
	valorTotal = valorTotal,
	metodo = metodo,
	statusPsp = statusPsp,
	status = status,
	split = split,
	qrCodePayload = qrCodePayload,
	qrCodeImagemBase64 = qrCodeImagemBase64
)
