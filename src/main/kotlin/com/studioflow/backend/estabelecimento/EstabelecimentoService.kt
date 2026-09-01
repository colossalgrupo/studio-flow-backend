package com.studioflow.backend.estabelecimento

import com.studioflow.backend.assinatura.AssinaturaService
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.dto.EstabelecimentoRequest
import com.studioflow.backend.plano.PlanoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class EstabelecimentoService(
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val planoRepository: PlanoRepository,
	private val assinaturaService: AssinaturaService
) {

	fun criar(usuarioDonoId: String, request: EstabelecimentoRequest): Estabelecimento {
		if (estabelecimentoRepository.existsByUsuarioDonoId(usuarioDonoId)) {
			throw BusinessException(HttpStatus.CONFLICT, "Usuário já possui um estabelecimento cadastrado")
		}

		val planoPadrao = planoRepository.findByNome("Standard")
			?: throw BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Plano padrão não configurado")

		val estabelecimento = estabelecimentoRepository.save(
			Estabelecimento(
				usuarioDonoId = usuarioDonoId,
				nome = request.nome,
				categoria = request.categoria,
				endereco = request.endereco.toEndereco(),
				planoId = planoPadrao.id!!
			)
		)

		assinaturaService.ativarPlano(estabelecimento.id!!, planoPadrao.id)

		return estabelecimento
	}

	fun buscarMeuEstabelecimento(usuarioDonoId: String): Estabelecimento =
		estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")

	fun atualizar(usuarioDonoId: String, request: EstabelecimentoRequest): Estabelecimento {
		val estabelecimento = buscarMeuEstabelecimento(usuarioDonoId)
		val atualizado = estabelecimento.copy(
			nome = request.nome,
			categoria = request.categoria,
			endereco = request.endereco.toEndereco()
		)
		return estabelecimentoRepository.save(atualizado)
	}

	fun alterarPlano(usuarioDonoId: String, planoId: String): Estabelecimento {
		val estabelecimento = buscarMeuEstabelecimento(usuarioDonoId)
		val plano = planoRepository.findById(planoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Plano não encontrado") }

		val atualizado = estabelecimentoRepository.save(estabelecimento.copy(planoId = plano.id!!))
		assinaturaService.ativarPlano(atualizado.id!!, plano.id)
		return atualizado
	}

	fun buscarPorId(id: String): Estabelecimento =
		estabelecimentoRepository.findById(id)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado") }

	fun buscar(categoria: CategoriaEstabelecimento?): List<Estabelecimento> =
		if (categoria != null) estabelecimentoRepository.findByCategoria(categoria)
		else estabelecimentoRepository.findAll()
}
