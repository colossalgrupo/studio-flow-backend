package com.studioflow.backend.plano

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "planos")
data class Plano(
	@Id val id: String? = null,
	@Indexed(unique = true) val nome: String,
	val precoMensal: BigDecimal,
	val taxaPlataformaPct: BigDecimal,
	/** null = sem limite de profissionais (plano Diamond) */
	val limiteProfissionais: Int?
)
