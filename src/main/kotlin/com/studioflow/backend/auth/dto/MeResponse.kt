package com.studioflow.backend.auth.dto

import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario

data class MeResponse(
	val id: String,
	val nome: String,
	val email: String,
	val tipoPerfil: TipoPerfil,
	val planoPreferido: String? = null
)

fun Usuario.toMeResponse(): MeResponse = MeResponse(
	id = id!!,
	nome = nome,
	email = email,
	tipoPerfil = tipoPerfil,
	planoPreferido = planoPreferido
)
