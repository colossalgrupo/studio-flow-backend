package com.studioflow.backend.estabelecimento.dto

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class EstabelecimentoRequest(
	@field:NotBlank(message = "Nome é obrigatório")
	val nome: String,

	@field:NotNull(message = "Categoria é obrigatória")
	val categoria: CategoriaEstabelecimento,

	@field:Valid
	@field:NotNull(message = "Endereço é obrigatório")
	val endereco: EnderecoDto,

	@field:NotBlank(message = "CPF ou CNPJ é obrigatório")
	val cpfCnpj: String = "",

	@field:NotNull(message = "Faturamento mensal é obrigatório")
	@field:DecimalMin(value = "0.01", message = "Faturamento mensal deve ser maior que zero")
	val faturamentoMensal: BigDecimal = BigDecimal.ZERO,

	/** Obrigatório quando cpfCnpj é CPF (pessoa física) — usado pra abrir a subconta Asaas. */
	val dataNascimento: LocalDate? = null,

	/** Obrigatório quando cpfCnpj é CNPJ: MEI, LIMITED, INDIVIDUAL ou ASSOCIATION (classificação da Asaas). */
	val companyType: String? = null
)
