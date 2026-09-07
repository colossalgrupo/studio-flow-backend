package com.studioflow.backend.bloqueio

import com.studioflow.backend.bloqueio.dto.BloqueioAgendaRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.profissional.ProfissionalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class BloqueioAgendaService(
	private val bloqueioAgendaRepository: BloqueioAgendaRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository
) {

	fun criar(usuarioDonoId: String, request: BloqueioAgendaRequest): BloqueioAgenda {
		val profissional = profissionalDoDono(usuarioDonoId, request.profissionalId)
		return bloqueioAgendaRepository.save(
			BloqueioAgenda(
				profissionalId = profissional.id!!,
				inicio = request.inicio,
				fim = request.fim,
				motivo = request.motivo
			)
		)
	}

	fun listarDoEstabelecimento(usuarioDonoId: String): List<BloqueioAgenda> {
		val estabelecimento = estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")
		val profissionaisIds = profissionalRepository.findByEstabelecimentoId(estabelecimento.id!!).mapNotNull { it.id }
		if (profissionaisIds.isEmpty()) return emptyList()
		return bloqueioAgendaRepository.findByProfissionalIdIn(profissionaisIds)
	}

	fun remover(usuarioDonoId: String, bloqueioId: String) {
		val bloqueio = bloqueioAgendaRepository.findById(bloqueioId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Bloqueio não encontrado") }
		profissionalDoDono(usuarioDonoId, bloqueio.profissionalId)
		bloqueioAgendaRepository.delete(bloqueio)
	}

	private fun profissionalDoDono(usuarioDonoId: String, profissionalId: String): com.studioflow.backend.profissional.Profissional {
		val estabelecimento = estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")
		val profissional = profissionalRepository.findById(profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		if (profissional.estabelecimentoId != estabelecimento.id) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Profissional não pertence ao seu estabelecimento")
		}
		return profissional
	}
}
