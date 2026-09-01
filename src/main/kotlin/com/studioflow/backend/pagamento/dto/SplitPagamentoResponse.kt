package com.studioflow.backend.pagamento.dto

import com.studioflow.backend.pagamento.SplitPagamento
import java.math.BigDecimal

data class SplitPagamentoResponse(
	val valorPlataforma: BigDecimal,
	val valorEstabelecimento: BigDecimal,
	val valorProfissional: BigDecimal
)

fun SplitPagamento.toResponse(): SplitPagamentoResponse = SplitPagamentoResponse(
	valorPlataforma = valorPlataforma,
	valorEstabelecimento = valorEstabelecimento,
	valorProfissional = valorProfissional
)
