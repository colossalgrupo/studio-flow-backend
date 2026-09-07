package com.studioflow.backend.lead

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface LeadRepository : MongoRepository<Lead, String> {
	fun existsByPlaceId(placeId: String): Boolean
	fun countByCidade(cidade: String): Long
	fun findByCidadeAndCategoria(cidade: String, categoria: CategoriaEstabelecimento, pageable: Pageable): Page<Lead>
	fun findByStatus(status: StatusLead, pageable: Pageable): Page<Lead>
}
