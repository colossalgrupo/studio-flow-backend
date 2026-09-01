package com.studioflow.backend.horario.dto

import com.studioflow.backend.horario.DiaSemana
import com.studioflow.backend.horario.HorarioDisponivel
import java.time.LocalTime

data class HorarioDisponivelResponse(
	val id: String,
	val profissionalId: String,
	val diaSemana: DiaSemana,
	val horaInicio: LocalTime,
	val horaFim: LocalTime
)

fun HorarioDisponivel.toResponse(): HorarioDisponivelResponse = HorarioDisponivelResponse(
	id = id!!,
	profissionalId = profissionalId,
	diaSemana = diaSemana,
	horaInicio = horaInicio,
	horaFim = horaFim
)
