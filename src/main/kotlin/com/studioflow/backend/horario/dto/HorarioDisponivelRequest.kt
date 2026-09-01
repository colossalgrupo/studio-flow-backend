package com.studioflow.backend.horario.dto

import com.studioflow.backend.horario.DiaSemana
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class HorarioDisponivelRequest(
	@field:NotNull(message = "Dia da semana é obrigatório")
	val diaSemana: DiaSemana,

	@field:NotNull(message = "Hora de início é obrigatória")
	val horaInicio: LocalTime,

	@field:NotNull(message = "Hora de fim é obrigatória")
	val horaFim: LocalTime
)
