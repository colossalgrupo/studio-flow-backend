package com.studioflow.backend.bloqueio

import org.springframework.data.mongodb.repository.MongoRepository

interface BloqueioAgendaRepository : MongoRepository<BloqueioAgenda, String> {
	fun findByProfissionalId(profissionalId: String): List<BloqueioAgenda>
	fun findByProfissionalIdIn(profissionalIds: List<String>): List<BloqueioAgenda>
}
