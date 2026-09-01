package com.studioflow.backend.servico

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "servicos")
data class Servico(
	@Id val id: String? = null,
	@Indexed val estabelecimentoId: String,
	val nome: String,
	val duracaoMin: Int,
	val precoBase: BigDecimal
)
