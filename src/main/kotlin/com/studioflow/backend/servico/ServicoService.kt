package com.studioflow.backend.servico

import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.servico.dto.ServicoRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ServicoService(
	private val servicoRepository: ServicoRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository
) {

	fun criar(usuarioDonoId: String, request: ServicoRequest): Servico {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		return servicoRepository.save(
			Servico(
				estabelecimentoId = estabelecimento.id!!,
				nome = request.nome,
				categoria = request.categoria,
				duracaoMin = request.duracaoMin,
				precoBase = request.precoBase,
				profissionaisIds = request.profissionaisIds,
				ativo = request.ativo
			)
		)
	}

	fun listarMeus(usuarioDonoId: String): List<Servico> {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		return servicoRepository.findByEstabelecimentoId(estabelecimento.id!!)
	}

	fun listarPorEstabelecimento(estabelecimentoId: String): List<Servico> =
		servicoRepository.findByEstabelecimentoId(estabelecimentoId)

	fun atualizar(usuarioDonoId: String, servicoId: String, request: ServicoRequest): Servico {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val servico = buscarDoEstabelecimento(servicoId, estabelecimento.id!!)

		return servicoRepository.save(
			servico.copy(
				nome = request.nome,
				categoria = request.categoria,
				duracaoMin = request.duracaoMin,
				precoBase = request.precoBase,
				profissionaisIds = request.profissionaisIds,
				ativo = request.ativo
			)
		)
	}

	fun remover(usuarioDonoId: String, servicoId: String) {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val servico = buscarDoEstabelecimento(servicoId, estabelecimento.id!!)
		servicoRepository.delete(servico)
	}

	fun buscarPorId(servicoId: String): Servico =
		servicoRepository.findById(servicoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Serviço não encontrado") }

	private fun estabelecimentoDoDono(usuarioDonoId: String) =
		estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")

	private fun buscarDoEstabelecimento(servicoId: String, estabelecimentoId: String): Servico {
		val servico = buscarPorId(servicoId)
		if (servico.estabelecimentoId != estabelecimentoId) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Serviço não pertence ao seu estabelecimento")
		}
		return servico
	}
}
