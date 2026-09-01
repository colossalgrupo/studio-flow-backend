package com.studioflow.backend.horario

import org.springframework.data.mongodb.repository.MongoRepository

interface HorarioDisponivelRepository : MongoRepository<HorarioDisponivel, String> {
	fun findByProfissionalId(profissionalId: String): List<HorarioDisponivel>
}
