package com.studioflow.backend.whatsapp.agente

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

data class MensagemConversa(
	/** "user" (contato) ou "assistant" (agente) */
	val papel: String,
	val texto: String,
	val enviadoEm: Instant = Instant.now()
)

/** Histórico de conversa do agente de vendas com um contato de WhatsApp. */
@Document(collection = "whatsapp_conversas")
data class Conversa(
	@Id val id: String? = null,
	@Indexed(unique = true) val waId: String,
	val nomeContato: String? = null,
	val mensagens: List<MensagemConversa> = emptyList(),
	val criadoEm: Instant = Instant.now(),
	val atualizadoEm: Instant = Instant.now()
)
