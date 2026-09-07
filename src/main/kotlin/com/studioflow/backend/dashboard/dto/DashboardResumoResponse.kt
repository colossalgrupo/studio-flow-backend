package com.studioflow.backend.dashboard.dto

import java.math.BigDecimal
import java.time.LocalDate

data class FaturamentoDiaResponse(
	val data: LocalDate,
	val valor: BigDecimal
)

data class DashboardResumoResponse(
	val totalAgendamentosPeriodo: Int,
	val faturamentoPeriodo: BigDecimal,
	val repassesPendentes: BigDecimal,
	val profissionaisAtivos: Int,
	val variacaoFaturamento: BigDecimal,
	val faturamentoPorDia: List<FaturamentoDiaResponse>
)
