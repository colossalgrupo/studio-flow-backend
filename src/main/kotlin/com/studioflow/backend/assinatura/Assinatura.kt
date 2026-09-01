package com.studioflow.backend.assinatura

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "assinaturas")
data class Assinatura(
	@Id val id: String? = null,
	@Indexed(unique = true) val estabelecimentoId: String,
	val planoId: String,
	val status: StatusAssinatura,
	/** Ciclo de cobrança atual, formato "yyyy-MM" */
	val cicloAtual: String
)
