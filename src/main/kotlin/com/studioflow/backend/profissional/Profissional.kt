package com.studioflow.backend.profissional

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "profissionais")
data class Profissional(
	@Id val id: String? = null,
	@Indexed val estabelecimentoId: String,
	val nome: String,
	val cpf: String,
	val email: String = "",
	val telefone: String = "",
	val especialidades: List<String>,
	val percentualComissao: BigDecimal,
	val periodicidadeRepasse: PeriodicidadeRepasse,
	val contaBancaria: ContaBancaria,
	val ativo: Boolean = true
)
