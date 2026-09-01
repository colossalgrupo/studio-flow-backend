package com.studioflow.backend.usuario

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "usuarios")
data class Usuario(
	@Id val id: String? = null,
	val nome: String,
	@Indexed(unique = true) val email: String,
	val senhaHash: String,
	val tipoPerfil: TipoPerfil
)
