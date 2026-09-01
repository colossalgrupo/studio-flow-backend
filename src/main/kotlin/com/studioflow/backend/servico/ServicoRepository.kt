package com.studioflow.backend.servico

import org.springframework.data.mongodb.repository.MongoRepository

interface ServicoRepository : MongoRepository<Servico, String> {
	fun findByEstabelecimentoId(estabelecimentoId: String): List<Servico>
}
