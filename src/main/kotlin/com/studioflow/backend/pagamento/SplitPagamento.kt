package com.studioflow.backend.pagamento

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "splits_pagamento")
data class SplitPagamento(
	@Id val id: String? = null,
	@Indexed(unique = true) val pagamentoId: String,
	val valorPlataforma: BigDecimal,
	val valorEstabelecimento: BigDecimal,
	val valorProfissional: BigDecimal,
	@Indexed val repasseId: String? = null
)
