package com.studioflow.backend.agendamento

import com.studioflow.backend.agendamento.dto.AgendamentoDetalhadoResponse
import com.studioflow.backend.agendamento.dto.AgendamentoRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.profissional.Profissional
import com.studioflow.backend.profissional.ProfissionalRepository
import com.studioflow.backend.servico.Servico
import com.studioflow.backend.servico.ServicoRepository
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class AgendamentoService(
	private val agendamentoRepository: AgendamentoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val servicoRepository: ServicoRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val usuarioRepository: UsuarioRepository
) {

	fun criar(clienteId: String, request: AgendamentoRequest): Agendamento {
		val profissional = profissionalRepository.findById(request.profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		val servico = servicoRepository.findById(request.servicoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Serviço não encontrado") }

		if (servico.estabelecimentoId != profissional.estabelecimentoId) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "Serviço não pertence ao estabelecimento do profissional")
		}

		try {
			return agendamentoRepository.save(
				Agendamento(
					clienteId = clienteId,
					profissionalId = profissional.id!!,
					servicoId = servico.id!!,
					dataHora = request.dataHora
				)
			)
		} catch (ex: DuplicateKeyException) {
			throw BusinessException(HttpStatus.CONFLICT, "Este profissional já possui um agendamento neste horário")
		}
	}

	fun listarMeus(clienteId: String): List<Agendamento> = agendamentoRepository.findByClienteId(clienteId)

	fun listarPorProfissionalDoDono(usuarioDonoId: String, profissionalId: String): List<Agendamento> {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val profissional = profissionalRepository.findById(profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		if (profissional.estabelecimentoId != estabelecimento.id) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Profissional não pertence ao seu estabelecimento")
		}
		return agendamentoRepository.findByProfissionalId(profissionalId)
	}

	fun listarPorEstabelecimentoDoDono(usuarioDonoId: String): List<AgendamentoDetalhadoResponse> {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val profissionais = profissionalRepository.findByEstabelecimentoId(estabelecimento.id!!)
		val profissionaisPorId = profissionais.associateBy { it.id }
		if (profissionais.isEmpty()) return emptyList()

		val agendamentos = agendamentoRepository.findByProfissionalIdIn(profissionais.mapNotNull { it.id })
		val servicosPorId = servicoRepository.findByEstabelecimentoId(estabelecimento.id).associateBy { it.id }
		val clientesPorId = usuarioRepository.findAllById(agendamentos.map { it.clienteId }.distinct()).associateBy { it.id }

		return agendamentos.map { agendamento ->
			enriquecer(agendamento, profissionaisPorId[agendamento.profissionalId], servicosPorId[agendamento.servicoId], clientesPorId[agendamento.clienteId])
		}
	}

	fun atualizarStatusDoEstabelecimento(usuarioDonoId: String, agendamentoId: String, status: StatusAgendamento): Agendamento {
		val estabelecimento = estabelecimentoDoDono(usuarioDonoId)
		val agendamento = buscarPorId(agendamentoId)
		val profissional = profissionalRepository.findById(agendamento.profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		if (profissional.estabelecimentoId != estabelecimento.id) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Agendamento não pertence ao seu estabelecimento")
		}
		return agendamentoRepository.save(agendamento.copy(status = status))
	}

	fun buscarPorId(id: String): Agendamento =
		agendamentoRepository.findById(id)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Agendamento não encontrado") }

	fun atualizarStatus(id: String, status: StatusAgendamento): Agendamento {
		val agendamento = buscarPorId(id)
		return agendamentoRepository.save(agendamento.copy(status = status))
	}

	private fun estabelecimentoDoDono(usuarioDonoId: String) =
		estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")

	private fun enriquecer(
		agendamento: Agendamento,
		profissional: Profissional?,
		servico: Servico?,
		cliente: Usuario?
	): AgendamentoDetalhadoResponse {
		val duracaoMin = servico?.duracaoMin?.toLong() ?: 0L
		return AgendamentoDetalhadoResponse(
			id = agendamento.id!!,
			clienteId = agendamento.clienteId,
			clienteNome = cliente?.nome ?: "Cliente",
			profissionalId = agendamento.profissionalId,
			profissionalNome = profissional?.nome ?: "",
			servicoId = agendamento.servicoId,
			servicoNome = servico?.nome ?: "",
			inicio = agendamento.dataHora,
			fim = agendamento.dataHora.plusMinutes(duracaoMin),
			valor = servico?.precoBase ?: java.math.BigDecimal.ZERO,
			status = agendamento.status
		)
	}
}
