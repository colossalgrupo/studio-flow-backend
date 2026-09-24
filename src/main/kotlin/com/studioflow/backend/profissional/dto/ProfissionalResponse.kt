package com.studioflow.backend.profissional.dto

import com.studioflow.backend.profissional.PeriodicidadeRepasse
import com.studioflow.backend.profissional.Profissional
import java.math.BigDecimal
import java.time.LocalDate

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
	val ativo: Boolean,
	val dataNascimento: LocalDate?,
	val faturamentoMensal: BigDecimal,
	// Nunca expor a apiKey da subconta aqui — só o status, que é seguro de mostrar.
	val asaasAccountStatus: String?
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
	ativo = ativo,
	dataNascimento = dataNascimento,
	faturamentoMensal = faturamentoMensal,
	asaasAccountStatus = asaasAccountStatus
)
