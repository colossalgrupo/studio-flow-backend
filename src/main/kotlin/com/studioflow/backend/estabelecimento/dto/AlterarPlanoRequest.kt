package com.studioflow.backend.estabelecimento.dto

import jakarta.validation.constraints.NotBlank

data class AlterarPlanoRequest(
	@field:NotBlank(message = "planoId é obrigatório")
	val planoId: String
)
