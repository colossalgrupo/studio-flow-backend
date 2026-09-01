package com.studioflow.backend.profissional

data class ContaBancaria(
	val chavePix: String,
	val banco: String,
	val agencia: String,
	val conta: String,
	val recebedorPspId: String? = null
)
