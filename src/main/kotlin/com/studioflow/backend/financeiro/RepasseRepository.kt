package com.studioflow.backend.financeiro

import org.springframework.data.mongodb.repository.MongoRepository

interface RepasseRepository : MongoRepository<Repasse, String> {
	fun findByProfissionalIdIn(profissionalIds: List<String>): List<Repasse>
}
