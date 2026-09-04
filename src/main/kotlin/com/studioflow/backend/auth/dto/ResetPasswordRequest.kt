package com.studioflow.backend.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
	@field:NotBlank(message = "Token é obrigatório")
	val token: String,

	@field:NotBlank(message = "Nova senha é obrigatória")
	@field:Size(min = 8, message = "Nova senha deve ter ao menos 8 caracteres")
	val newPassword: String
)
