package com.studioflow.backend.profissional

import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.plano.PlanoRepository
import com.studioflow.backend.profissional.dto.ProfissionalRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ProfissionalService(
	private val profissionalRepository: ProfissionalRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val planoRepository: PlanoRepository
) {

	fun criar(usuarioDonoId: String, request: ProfissionalRequest): Profissional {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val plano = planoRepository.findById(estabelecimento.planoId)
			.orElseThrow { BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Plano do estabelecimento não encontrado") }

		val limite = plano.limiteProfissionais
		if (limite != null) {
			val quantidadeAtual = profissionalRepository.countByEstabelecimentoId(estabelecimento.id!!)
			if (quantidadeAtual >= limite) {
				throw BusinessException(
					HttpStatus.FORBIDDEN,
					"Limite de profissionais do plano ${plano.nome} atingido ($limite). Faça upgrade do plano para cadastrar mais profissionais."
				)
			}
		}

		return profissionalRepository.save(
			Profissional(
				estabelecimentoId = estabelecimento.id!!,
				nome = request.nome,
				cpf = request.cpf,
				email = request.email,
				telefone = request.telefone,
				especialidades = request.especialidades,
				percentualComissao = request.percentualComissao,
				periodicidadeRepasse = request.periodicidadeRepasse,
				contaBancaria = request.contaBancaria.toContaBancaria(),
				ativo = request.ativo
			)
		)
	}

	fun listarMeus(usuarioDonoId: String): List<Profissional> {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		return profissionalRepository.findByEstabelecimentoId(estabelecimento.id!!)
	}

	fun atualizar(usuarioDonoId: String, profissionalId: String, request: ProfissionalRequest): Profissional {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val profissional = buscarDoEstabelecimento(profissionalId, estabelecimento.id!!)

		return profissionalRepository.save(
			profissional.copy(
				nome = request.nome,
				cpf = request.cpf,
				email = request.email,
				telefone = request.telefone,
				especialidades = request.especialidades,
				percentualComissao = request.percentualComissao,
				periodicidadeRepasse = request.periodicidadeRepasse,
				contaBancaria = request.contaBancaria.toContaBancaria(),
				ativo = request.ativo
			)
		)
	}

	fun remover(usuarioDonoId: String, profissionalId: String) {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val profissional = buscarDoEstabelecimento(profissionalId, estabelecimento.id!!)
		profissionalRepository.delete(profissional)
	}

	fun buscarPorId(profissionalId: String): Profissional =
		profissionalRepository.findById(profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }

	fun buscarPublico(estabelecimentoId: String?, especialidade: String?): List<Profissional> =
		when {
			estabelecimentoId != null -> profissionalRepository.findByEstabelecimentoId(estabelecimentoId)
			especialidade != null -> profissionalRepository.findByEspecialidadesContaining(especialidade)
			else -> profissionalRepository.findAll()
		}

	private fun estabelecimentoDoDono(usuarioDonoId: String) =
		estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")

	private fun buscarDoEstabelecimento(profissionalId: String, estabelecimentoId: String): Profissional {
		val profissional = buscarPorId(profissionalId)
		if (profissional.estabelecimentoId != estabelecimentoId) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Profissional não pertence ao seu estabelecimento")
		}
		return profissional
	}
}
