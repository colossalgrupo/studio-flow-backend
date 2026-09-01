package com.studioflow.backend.estabelecimento.dto

import com.studioflow.backend.estabelecimento.Endereco
import jakarta.validation.constraints.NotBlank

data class EnderecoDto(
	@field:NotBlank(message = "Logradouro é obrigatório")
	val logradouro: String,

	@field:NotBlank(message = "Número é obrigatório")
	val numero: String,

	@field:NotBlank(message = "Bairro é obrigatório")
	val bairro: String,

	@field:NotBlank(message = "Cidade é obrigatória")
	val cidade: String,

	@field:NotBlank(message = "Estado é obrigatório")
	val estado: String,

	@field:NotBlank(message = "CEP é obrigatório")
	val cep: String
) {
	fun toEndereco(): Endereco = Endereco(
		logradouro = logradouro,
		numero = numero,
		bairro = bairro,
		cidade = cidade,
		estado = estado,
		cep = cep
	)

	companion object {
		fun from(endereco: Endereco): EnderecoDto = EnderecoDto(
			logradouro = endereco.logradouro,
			numero = endereco.numero,
			bairro = endereco.bairro,
			cidade = endereco.cidade,
			estado = endereco.estado,
			cep = endereco.cep
		)
	}
}
