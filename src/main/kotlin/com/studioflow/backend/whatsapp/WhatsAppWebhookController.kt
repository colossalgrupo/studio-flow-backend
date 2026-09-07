package com.studioflow.backend.whatsapp

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
 * de handshake (GET) e o corpo bruto de cada evento (POST) é logado/persistido
 * para depuração, até o agente de conversa (Claude) ser conectado.
 */
@RestController
@RequestMapping("/api/whatsapp")
class WhatsAppWebhookController(
	private val eventoRepository: WhatsAppEventoRepository,
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
		log.info("Evento recebido do WhatsApp: {}", payload)
		eventoRepository.save(WhatsAppEvento(payload = payload))
		return ResponseEntity.ok().build()
	}
}
