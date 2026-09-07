package com.studioflow.backend.profissional.dto

import com.studioflow.backend.profissional.PeriodicidadeRepasse
import com.studioflow.backend.profissional.Profissional
import java.math.BigDecimal

data class ProfissionalResponse(
	val id: String,
	val estabelecimentoId: String,
	val nome: String,
	val cpf: String,
	val email: String,
	val telefone: String,
	val especialidades: List<String>,
	val percentualComissao: BigDecimal,
	val periodicidadeRepasse: PeriodicidadeRepasse,
	val contaBancaria: ContaBancariaDto,
	val ativo: Boolean
)

fun Profissional.toResponse(): ProfissionalResponse = ProfissionalResponse(
	id = id!!,
	estabelecimentoId = estabelecimentoId,
	nome = nome,
	cpf = cpf,
	email = email,
	telefone = telefone,
	especialidades = especialidades,
	percentualComissao = percentualComissao,
	periodicidadeRepasse = periodicidadeRepasse,
	contaBancaria = ContaBancariaDto.from(contaBancaria),
	ativo = ativo
)
