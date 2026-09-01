package com.studioflow.backend.agendamento

import org.springframework.data.mongodb.repository.MongoRepository

interface AgendamentoRepository : MongoRepository<Agendamento, String> {
	fun findByClienteId(clienteId: String): List<Agendamento>
	fun findByProfissionalId(profissionalId: String): List<Agendamento>
}
