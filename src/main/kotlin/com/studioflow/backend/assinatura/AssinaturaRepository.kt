package com.studioflow.backend.assinatura

import org.springframework.data.mongodb.repository.MongoRepository

interface AssinaturaRepository : MongoRepository<Assinatura, String> {
	fun findByEstabelecimentoId(estabelecimentoId: String): Assinatura?
}
