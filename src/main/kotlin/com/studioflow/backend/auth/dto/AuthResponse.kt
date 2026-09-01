package com.studioflow.backend.auth.dto

import com.studioflow.backend.usuario.TipoPerfil

data class AuthResponse(
	val token: String,
	val tipoPerfil: TipoPerfil,
	val nome: String,
	val email: String
)
