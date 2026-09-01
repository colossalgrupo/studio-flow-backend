package com.studioflow.backend.pagamento

import org.springframework.data.mongodb.repository.MongoRepository

interface PagamentoRepository : MongoRepository<Pagamento, String> {
	fun findByAgendamentoId(agendamentoId: String): Pagamento?
}
