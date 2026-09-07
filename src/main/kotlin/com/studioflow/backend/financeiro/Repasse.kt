package com.studioflow.backend.financeiro

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Representa um repasse já realizado (transferência efetivamente feita ao profissional).
 * Repasses pendentes não são persistidos — são calculados sob demanda a partir dos
 * splits de pagamento ainda não alocados a nenhum repasse (ver FinanceiroService).
 */
@Document(collection = "repasses")
data class Repasse(
	@Id val id: String? = null,
	@Indexed val profissionalId: String,
	val periodoInicio: LocalDate,
	val periodoFim: LocalDate,
	val valorBruto: BigDecimal,
	val valorComissaoPlataforma: BigDecimal,
	val valorLiquido: BigDecimal,
	val status: StatusRepasse = StatusRepasse.REALIZADO,
	val dataPrevista: LocalDate,
	val dataRealizado: LocalDate? = null
)
