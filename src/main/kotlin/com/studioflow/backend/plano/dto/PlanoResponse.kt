package com.studioflow.backend.plano.dto

import com.studioflow.backend.plano.Plano
import java.math.BigDecimal

data class PlanoResponse(
	val id: String,
	val nome: String,
	val precoMensal: BigDecimal,
	val taxaPlataformaPct: BigDecimal,
	val limiteProfissionais: Int?
)

fun Plano.toResponse(): PlanoResponse = PlanoResponse(
	id = id!!,
	nome = nome,
	precoMensal = precoMensal,
	taxaPlataformaPct = taxaPlataformaPct,
	limiteProfissionais = limiteProfissionais
)
