package com.studioflow.backend.horario

import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.horario.dto.HorarioDisponivelRequest
import com.studioflow.backend.profissional.Profissional
import com.studioflow.backend.profissional.ProfissionalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class HorarioDisponivelService(
	private val horarioDisponivelRepository: HorarioDisponivelRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository
) {

	fun criar(usuarioDonoId: String, profissionalId: String, request: HorarioDisponivelRequest): HorarioDisponivel {
		val profissional = profissionalDoDono(usuarioDonoId, profissionalId)
		validarIntervalo(request)

		return horarioDisponivelRepository.save(
			HorarioDisponivel(
				profissionalId = profissional.id!!,
				diaSemana = request.diaSemana,
				horaInicio = request.horaInicio,
				horaFim = request.horaFim
			)
		)
	}

	fun listarPorProfissional(profissionalId: String): List<HorarioDisponivel> =
		horarioDisponivelRepository.findByProfissionalId(profissionalId)

	fun atualizar(usuarioDonoId: String, horarioId: String, request: HorarioDisponivelRequest): HorarioDisponivel {
		val horario = buscarPorId(horarioId)
		profissionalDoDono(usuarioDonoId, horario.profissionalId)
		validarIntervalo(request)

		return horarioDisponivelRepository.save(
			horario.copy(diaSemana = request.diaSemana, horaInicio = request.horaInicio, horaFim = request.horaFim)
		)
	}

	fun remover(usuarioDonoId: String, horarioId: String) {
		val horario = buscarPorId(horarioId)
		profissionalDoDono(usuarioDonoId, horario.profissionalId)
		horarioDisponivelRepository.delete(horario)
	}

	private fun buscarPorId(id: String) =
		horarioDisponivelRepository.findById(id)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Horário não encontrado") }

	private fun validarIntervalo(request: HorarioDisponivelRequest) {
		if (!request.horaInicio.isBefore(request.horaFim)) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "Hora de início deve ser anterior à hora de fim")
		}
	}

	private fun profissionalDoDono(usuarioDonoId: String, profissionalId: String): Profissional {
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
