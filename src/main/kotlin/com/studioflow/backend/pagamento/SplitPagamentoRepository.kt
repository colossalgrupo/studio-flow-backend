package com.studioflow.backend.pagamento

import org.springframework.data.mongodb.repository.MongoRepository

interface SplitPagamentoRepository : MongoRepository<SplitPagamento, String> {
	fun findByPagamentoId(pagamentoId: String): SplitPagamento?
}
