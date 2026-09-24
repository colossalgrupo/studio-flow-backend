package com.studioflow.backend.whatsapp.agente

import org.springframework.data.mongodb.repository.MongoRepository

interface ConversaRepository : MongoRepository<Conversa, String> {
	fun findByWaId(waId: String): Conversa?
}
