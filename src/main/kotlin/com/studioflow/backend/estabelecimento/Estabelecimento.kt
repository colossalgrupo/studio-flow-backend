package com.studioflow.backend.estabelecimento

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "estabelecimentos")
data class Estabelecimento(
	@Id val id: String? = null,
	@Indexed(unique = true) val usuarioDonoId: String,
	val nome: String,
	val categoria: CategoriaEstabelecimento,
	val endereco: Endereco,
	val planoId: String
)
