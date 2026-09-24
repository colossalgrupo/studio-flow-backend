package com.studioflow.backend.profissional

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDate

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
	val ativo: Boolean = true,
	val dataNascimento: LocalDate? = null,
	val faturamentoMensal: BigDecimal = BigDecimal.ZERO,
	// walletId da subconta Asaas do profissional fica em contaBancaria.recebedorPspId.
	@Indexed val asaasAccountId: String? = null,
	val asaasAccountApiKeyCriptografada: String? = null,
	val asaasAccountStatus: String? = null
)
