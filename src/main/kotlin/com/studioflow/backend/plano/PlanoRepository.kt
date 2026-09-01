package com.studioflow.backend.plano

import org.springframework.data.mongodb.repository.MongoRepository

interface PlanoRepository : MongoRepository<Plano, String> {
	fun findByNome(nome: String): Plano?
}
