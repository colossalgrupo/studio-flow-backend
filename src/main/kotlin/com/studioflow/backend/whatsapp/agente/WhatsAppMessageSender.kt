package com.studioflow.backend.whatsapp.agente

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

/** Envia mensagens de texto livre via WhatsApp Cloud API (Graph API). */
@Service
class WhatsAppMessageSender(
	@Value("\${studioflow.whatsapp.access-token:}") private val accessToken: String,
	@Value("\${studioflow.whatsapp.phone-number-id:}") private val phoneNumberId: String,
	private val restClient: RestClient = RestClient.create("https://graph.facebook.com/v21.0")
) {
	private val log = LoggerFactory.getLogger(WhatsAppMessageSender::class.java)

	fun enviarTexto(paraWaId: String, texto: String) {
		if (accessToken.isBlank() || phoneNumberId.isBlank()) {
			log.warn("WhatsApp não configurado (access-token/phone-number-id ausentes) — mensagem para {} não enviada", paraWaId)
			return
		}
		try {
			restClient.post()
				.uri("/$phoneNumberId/messages")
				.header("Authorization", "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.body(
					mapOf(
						"messaging_product" to "whatsapp",
						"to" to paraWaId,
						"type" to "text",
						"text" to mapOf("body" to texto)
					)
				)
				.retrieve()
				.toBodilessEntity()
		} catch (ex: Exception) {
			log.error("Falha ao enviar mensagem de WhatsApp para {}: {}", paraWaId, ex.message, ex)
		}
	}
}
