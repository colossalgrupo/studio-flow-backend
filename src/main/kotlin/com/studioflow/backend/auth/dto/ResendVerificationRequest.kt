package com.studioflow.backend.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResendVerificationRequest(
	@field:NotBlank(message = "E-mail é obrigatório")
	@field:Email(message = "E-mail inválido")
	val email: String
)
