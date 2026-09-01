package com.studioflow.backend.servico.dto

import com.studioflow.backend.servico.Servico
import java.math.BigDecimal

data class ServicoResponse(
	val id: String,
	val estabelecimentoId: String,
	val nome: String,
	val duracaoMin: Int,
	val precoBase: BigDecimal
)

fun Servico.toResponse(): ServicoResponse = ServicoResponse(
	id = id!!,
	estabelecimentoId = estabelecimentoId,
	nome = nome,
	duracaoMin = duracaoMin,
	precoBase = precoBase
)
