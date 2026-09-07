package com.studioflow.backend.bloqueio

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "bloqueios_agenda")
data class BloqueioAgenda(
	@Id val id: String? = null,
	@Indexed val profissionalId: String,
	val inicio: LocalDateTime,
	val fim: LocalDateTime,
	val motivo: String
)
