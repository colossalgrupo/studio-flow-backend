package com.studioflow.backend.estabelecimento.dto

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import com.studioflow.backend.estabelecimento.Estabelecimento

data class EstabelecimentoResponse(
	val id: String,
	val usuarioDonoId: String,
	val nome: String,
	val categoria: CategoriaEstabelecimento,
	val endereco: EnderecoDto,
	val planoId: String
)

fun Estabelecimento.toResponse(): EstabelecimentoResponse = EstabelecimentoResponse(
	id = id!!,
	usuarioDonoId = usuarioDonoId,
	nome = nome,
	categoria = categoria,
	endereco = EnderecoDto.from(endereco),
	planoId = planoId
)
