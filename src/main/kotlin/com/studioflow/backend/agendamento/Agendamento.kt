package com.studioflow.backend.agendamento

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "agendamentos")
@CompoundIndex(name = "profissional_data_hora_unique", def = "{'profissionalId': 1, 'dataHora': 1}", unique = true)
data class Agendamento(
	@Id val id: String? = null,
	val clienteId: String,
	val profissionalId: String,
	val servicoId: String,
	val dataHora: LocalDateTime,
	val status: StatusAgendamento = StatusAgendamento.PENDENTE_PAGAMENTO
)
