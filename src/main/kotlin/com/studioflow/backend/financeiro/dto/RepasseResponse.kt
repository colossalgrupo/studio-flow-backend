package com.studioflow.backend.financeiro.dto

import com.studioflow.backend.financeiro.Repasse
import com.studioflow.backend.financeiro.StatusRepasse
import java.math.BigDecimal
import java.time.LocalDate

data class RepasseResponse(
	val id: String,
	val profissionalId: String,
	val profissionalNome: String,
	val periodoInicio: LocalDate,
	val periodoFim: LocalDate,
	val valorBruto: BigDecimal,
	val valorComissaoPlataforma: BigDecimal,
	val valorLiquido: BigDecimal,
	val status: StatusRepasse,
	val dataPrevista: LocalDate,
	val dataRealizado: LocalDate?
)

fun Repasse.toResponse(profissionalNome: String): RepasseResponse = RepasseResponse(
	id = id!!,
	profissionalId = profissionalId,
	profissionalNome = profissionalNome,
	periodoInicio = periodoInicio,
	periodoFim = periodoFim,
	valorBruto = valorBruto,
	valorComissaoPlataforma = valorComissaoPlataforma,
	valorLiquido = valorLiquido,
	status = status,
	dataPrevista = dataPrevista,
	dataRealizado = dataRealizado
)
