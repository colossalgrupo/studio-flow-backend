package com.studioflow.backend.profissional.dto

import com.studioflow.backend.profissional.PeriodicidadeRepasse
import com.studioflow.backend.profissional.Profissional
import java.math.BigDecimal

data class ProfissionalResponse(
	val id: String,
	val estabelecimentoId: String,
	val nome: String,
	val cpf: String,
	val especialidades: List<String>,
	val percentualComissao: BigDecimal,
	val periodicidadeRepasse: PeriodicidadeRepasse,
	val contaBancaria: ContaBancariaDto
)

fun Profissional.toResponse(): ProfissionalResponse = ProfissionalResponse(
	id = id!!,
	estabelecimentoId = estabelecimentoId,
	nome = nome,
	cpf = cpf,
	especialidades = especialidades,
	percentualComissao = percentualComissao,
	periodicidadeRepasse = periodicidadeRepasse,
	contaBancaria = ContaBancariaDto.from(contaBancaria)
)
