package com.studioflow.backend.profissional.dto

import com.studioflow.backend.profissional.ContaBancaria
import jakarta.validation.constraints.NotBlank

data class ContaBancariaDto(
	@field:NotBlank(message = "Chave Pix é obrigatória")
	val chavePix: String,

	@field:NotBlank(message = "Banco é obrigatório")
	val banco: String,

	@field:NotBlank(message = "Agência é obrigatória")
	val agencia: String,

	@field:NotBlank(message = "Conta é obrigatória")
	val conta: String,

	val recebedorPspId: String? = null
) {
	fun toContaBancaria(): ContaBancaria = ContaBancaria(
		chavePix = chavePix,
		banco = banco,
		agencia = agencia,
		conta = conta,
		recebedorPspId = recebedorPspId
	)

	companion object {
		fun from(contaBancaria: ContaBancaria): ContaBancariaDto = ContaBancariaDto(
			chavePix = contaBancaria.chavePix,
			banco = contaBancaria.banco,
			agencia = contaBancaria.agencia,
			conta = contaBancaria.conta,
			recebedorPspId = contaBancaria.recebedorPspId
		)
	}
}
