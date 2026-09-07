package com.studioflow.backend.financeiro.dto

import com.studioflow.backend.pagamento.MetodoPagamento
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransacaoResponse(
	val id: String,
	val agendamentoId: String,
	val data: LocalDateTime,
	val metodo: MetodoPagamento,
	val clienteNome: String,
	val profissionalId: String,
	val profissionalNome: String,
	val servicoId: String,
	val servicoNome: String,
	val valorTotal: BigDecimal,
	val taxaPlataforma: BigDecimal,
	val valorEstabelecimento: BigDecimal,
	val valorProfissional: BigDecimal
)
