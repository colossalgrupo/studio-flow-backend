package com.studioflow.backend.estabelecimento.dto

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import com.studioflow.backend.estabelecimento.Estabelecimento
import java.math.BigDecimal

data class EstabelecimentoResponse(
	val id: String,
	val usuarioDonoId: String,
	val nome: String,
	val categoria: CategoriaEstabelecimento,
	val endereco: EnderecoDto,
	val planoId: String,
	val cpfCnpj: String,
	val faturamentoMensal: BigDecimal,
	// Nunca expor a apiKey da subconta aqui — só walletId/status, que são seguros de mostrar.
	val asaasWalletId: String?,
	val asaasAccountStatus: String?
)

fun Estabelecimento.toResponse(): EstabelecimentoResponse = EstabelecimentoResponse(
	id = id!!,
	usuarioDonoId = usuarioDonoId,
	nome = nome,
	categoria = categoria,
	endereco = EnderecoDto.from(endereco),
	planoId = planoId,
	cpfCnpj = cpfCnpj,
	faturamentoMensal = faturamentoMensal,
	asaasWalletId = asaasWalletId,
	asaasAccountStatus = asaasAccountStatus
)
