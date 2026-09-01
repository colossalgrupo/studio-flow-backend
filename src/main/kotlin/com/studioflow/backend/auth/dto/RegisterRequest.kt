package com.studioflow.backend.auth.dto

import com.studioflow.backend.usuario.TipoPerfil
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
	@field:NotBlank(message = "Nome é obrigatório")
	val nome: String,

	@field:NotBlank(message = "E-mail é obrigatório")
	@field:Email(message = "E-mail inválido")
	val email: String,

	@field:NotBlank(message = "Senha é obrigatória")
	@field:Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
	val senha: String,

	@field:NotNull(message = "Tipo de perfil é obrigatório")
	val tipoPerfil: TipoPerfil
)
