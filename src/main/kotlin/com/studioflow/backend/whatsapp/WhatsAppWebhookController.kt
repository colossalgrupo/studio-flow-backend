package com.studioflow.backend.whatsapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.studioflow.backend.whatsapp.agente.AgenteVendasService
import com.studioflow.backend.whatsapp.dto.WhatsAppWebhookPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Webhook público da WhatsApp Cloud API (Meta). Sem autenticação JWT — a Meta chama
 * este endpoint diretamente; a verificação é feita pelo hub.verify_token na etapa
 * de handshake (GET). Cada evento (POST) é persistido bruto para auditoria e, quando
 * traz uma mensagem de texto recebida, é repassado ao agente de vendas (Claude).
 */
@RestController
@RequestMapping("/api/whatsapp")
class WhatsAppWebhookController(
	private val eventoRepository: WhatsAppEventoRepository,
	private val agenteVendasService: AgenteVendasService,
	private val objectMapper: ObjectMapper,
	@Value("\${studioflow.whatsapp.verify-token}") private val verifyToken: String
) {
	private val log = LoggerFactory.getLogger(WhatsAppWebhookController::class.java)

	@GetMapping("/webhook")
	fun verificar(
		@RequestParam("hub.mode") mode: String,
		@RequestParam("hub.verify_token") token: String,
		@RequestParam("hub.challenge") challenge: String
	): ResponseEntity<String> {
		return if (mode == "subscribe" && token == verifyToken) {
			log.info("Webhook do WhatsApp verificado com sucesso pela Meta")
			ResponseEntity.ok(challenge)
		} else {
			log.warn("Falha na verificação do webhook do WhatsApp: mode={} tokenConfere={}", mode, token == verifyToken)
			ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}

	@PostMapping("/webhook")
	fun receber(@RequestBody payload: String): ResponseEntity<Void> {
		eventoRepository.save(WhatsAppEvento(payload = payload))

		try {
			processarMensagensRecebidas(payload)
		} catch (ex: Exception) {
			log.error("Falha ao processar evento do WhatsApp: {}", ex.message, ex)
		}

		return ResponseEntity.ok().build()
	}

	private fun processarMensagensRecebidas(payload: String) {
		val evento = objectMapper.readValue(payload, WhatsAppWebhookPayload::class.java)

		for (entry in evento.entry) {
			for (change in entry.changes) {
				if (change.field != "messages" || change.value.messages.isEmpty()) continue

				val nomesPorWaId = change.value.contacts.associate { it.waId to it.profile.name }

				for (mensagem in change.value.messages) {
					if (mensagem.type != "text" || mensagem.text == null) continue

					val waId = mensagem.from
					val nomeContato = nomesPorWaId[waId]
					log.info("Mensagem recebida de {} ({}): {}", waId, nomeContato, mensagem.text.body)
					agenteVendasService.processarMensagemRecebida(waId, nomeContato, mensagem.text.body)
				}
			}
		}
	}
}
