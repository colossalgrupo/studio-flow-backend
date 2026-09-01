package com.studioflow.backend.servico.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class ServicoRequest(
	@field:NotBlank(message = "Nome é obrigatório")
	val nome: String,

	@field:NotNull(message = "Duração é obrigatória")
	@field:Min(value = 1, message = "Duração deve ser de ao menos 1 minuto")
	val duracaoMin: Int,

	@field:NotNull(message = "Preço base é obrigatório")
	@field:DecimalMin(value = "0.0", inclusive = false, message = "Preço base deve ser maior que 0")
	val precoBase: BigDecimal
)
