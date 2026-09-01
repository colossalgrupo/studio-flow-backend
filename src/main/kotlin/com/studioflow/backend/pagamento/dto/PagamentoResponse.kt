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
	val split: SplitPagamentoResponse?
)

fun Pagamento.toResponse(split: SplitPagamentoResponse?): PagamentoResponse = PagamentoResponse(
	id = id!!,
	agendamentoId = agendamentoId,
	valorTotal = valorTotal,
	metodo = metodo,
	statusPsp = statusPsp,
	status = status,
	split = split
)
