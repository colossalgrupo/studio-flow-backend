package com.studioflow.backend.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
	@field:NotBlank(message = "E-mail é obrigatório")
	@field:Email(message = "E-mail inválido")
	val email: String,

	@field:NotBlank(message = "Senha é obrigatória")
	val senha: String
)
