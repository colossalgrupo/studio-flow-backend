package com.studioflow.backend.estabelecimento

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDate

@Document(collection = "estabelecimentos")
data class Estabelecimento(
	@Id val id: String? = null,
	@Indexed(unique = true) val usuarioDonoId: String,
	val nome: String,
	val categoria: CategoriaEstabelecimento,
	val endereco: Endereco,
	val planoId: String,
	val cpfCnpj: String = "",
	val faturamentoMensal: BigDecimal = BigDecimal.ZERO,
	val dataNascimento: LocalDate? = null,
	val companyType: String? = null,
	// Dados da subconta Asaas usada pra receber a parte do split que cabe ao estabelecimento.
	@Indexed val asaasAccountId: String? = null,
	val asaasWalletId: String? = null,
	val asaasAccountApiKeyCriptografada: String? = null,
	val asaasAccountStatus: String? = null
)
