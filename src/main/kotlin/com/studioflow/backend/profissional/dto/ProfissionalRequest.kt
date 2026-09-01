package com.studioflow.backend.profissional.dto

import com.studioflow.backend.profissional.PeriodicidadeRepasse
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class ProfissionalRequest(
	@field:NotBlank(message = "Nome é obrigatório")
	val nome: String,

	@field:NotBlank(message = "CPF é obrigatório")
	val cpf: String,

	@field:NotEmpty(message = "Informe ao menos uma especialidade")
	val especialidades: List<String>,

	@field:NotNull(message = "Percentual de comissão é obrigatório")
	@field:DecimalMin(value = "0.0", message = "Percentual de comissão deve ser maior ou igual a 0")
	@field:DecimalMax(value = "100.0", message = "Percentual de comissão deve ser menor ou igual a 100")
	val percentualComissao: BigDecimal,

	@field:NotNull(message = "Periodicidade de repasse é obrigatória")
	val periodicidadeRepasse: PeriodicidadeRepasse,

	@field:Valid
	@field:NotNull(message = "Conta bancária é obrigatória")
	val contaBancaria: ContaBancariaDto
)
