package com.studioflow.backend.whatsapp

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/** Payload bruto recebido do webhook da WhatsApp Cloud API, guardado para depuração/auditoria. */
@Document(collection = "whatsapp_eventos")
data class WhatsAppEvento(
	@Id val id: String? = null,
	val payload: String,
	val recebidoEm: Instant = Instant.now()
)
