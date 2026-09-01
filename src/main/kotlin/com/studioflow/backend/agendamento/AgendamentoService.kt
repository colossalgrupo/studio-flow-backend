package com.studioflow.backend.agendamento

import com.studioflow.backend.agendamento.dto.AgendamentoRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.profissional.ProfissionalRepository
import com.studioflow.backend.servico.ServicoRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class AgendamentoService(
	private val agendamentoRepository: AgendamentoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val servicoRepository: ServicoRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository
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
		val estabelecimento = estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")
		val profissional = profissionalRepository.findById(profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		if (profissional.estabelecimentoId != estabelecimento.id) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Profissional não pertence ao seu estabelecimento")
		}
		return agendamentoRepository.findByProfissionalId(profissionalId)
	}

	fun buscarPorId(id: String): Agendamento =
		agendamentoRepository.findById(id)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Agendamento não encontrado") }

	fun atualizarStatus(id: String, status: StatusAgendamento): Agendamento {
		val agendamento = buscarPorId(id)
		return agendamentoRepository.save(agendamento.copy(status = status))
	}
}
