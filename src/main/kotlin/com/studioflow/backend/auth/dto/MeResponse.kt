package com.studioflow.backend.auth.dto

import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario

data class MeResponse(
	val id: String,
	val nome: String,
	val email: String,
	val tipoPerfil: TipoPerfil
)

fun Usuario.toMeResponse(): MeResponse = MeResponse(
	id = id!!,
	nome = nome,
	email = email,
	tipoPerfil = tipoPerfil
)
