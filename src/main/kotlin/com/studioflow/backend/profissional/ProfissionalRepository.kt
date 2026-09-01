package com.studioflow.backend.profissional

import org.springframework.data.mongodb.repository.MongoRepository

interface ProfissionalRepository : MongoRepository<Profissional, String> {
	fun findByEstabelecimentoId(estabelecimentoId: String): List<Profissional>
	fun countByEstabelecimentoId(estabelecimentoId: String): Long
	fun findByEspecialidadesContaining(especialidade: String): List<Profissional>
}
