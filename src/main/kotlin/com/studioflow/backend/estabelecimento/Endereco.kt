package com.studioflow.backend.estabelecimento

data class Endereco(
	val logradouro: String,
	val numero: String,
	val bairro: String,
	val cidade: String,
	val estado: String,
	val cep: String
)
