package com.studioflow.backend.servico.dto

import com.studioflow.backend.servico.Servico
import java.math.BigDecimal

data class ServicoResponse(
	val id: String,
	val estabelecimentoId: String,
	val nome: String,
	val categoria: String,
	val duracaoMin: Int,
	val precoBase: BigDecimal,
	val profissionaisIds: List<String>,
	val ativo: Boolean
)

fun Servico.toResponse(): ServicoResponse = ServicoResponse(
	id = id!!,
	estabelecimentoId = estabelecimentoId,
	nome = nome,
	categoria = categoria,
	duracaoMin = duracaoMin,
	precoBase = precoBase,
	profissionaisIds = profissionaisIds,
	ativo = ativo
)
