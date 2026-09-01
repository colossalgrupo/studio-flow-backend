package com.studioflow.backend.estabelecimento.dto

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EstabelecimentoRequest(
	@field:NotBlank(message = "Nome é obrigatório")
	val nome: String,

	@field:NotNull(message = "Categoria é obrigatória")
	val categoria: CategoriaEstabelecimento,

	@field:Valid
	@field:NotNull(message = "Endereço é obrigatório")
	val endereco: EnderecoDto
)
