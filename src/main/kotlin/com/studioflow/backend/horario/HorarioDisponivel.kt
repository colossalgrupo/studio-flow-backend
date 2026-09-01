package com.studioflow.backend.horario

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalTime

@Document(collection = "horarios_disponiveis")
data class HorarioDisponivel(
	@Id val id: String? = null,
	@Indexed val profissionalId: String,
	val diaSemana: DiaSemana,
	val horaInicio: LocalTime,
	val horaFim: LocalTime
)
