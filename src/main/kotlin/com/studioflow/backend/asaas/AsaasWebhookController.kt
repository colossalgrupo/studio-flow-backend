package com.studioflow.backend.asaas

import com.fasterxml.jackson.databind.ObjectMapper
import com.studioflow.backend.asaas.dto.AsaasWebhookPayload
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.pagamento.PagamentoService
import com.studioflow.backend.profissional.ProfissionalRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Webhook de eventos de conta da Asaas (ACCOUNT_STATUS_*) — atualiza o status local
 * da subconta do estabelecimento/profissional (ver AsaasOnboardingService). A Asaas
 * reenvia o mesmo evento em caso de falha (at-least-once), então isso precisa ser
 * idempotente — aqui é só uma atualização de status, então já é seguro por natureza.
 */
@RestController
@RequestMapping("/api/asaas")
class AsaasWebhookController(
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val pagamentoService: PagamentoService,
	private val objectMapper: ObjectMapper,
	@Value("\${studioflow.asaas.webhook-token:}") private val webhookToken: String
) {
	private val log = LoggerFactory.getLogger(AsaasWebhookController::class.java)

	@PostMapping("/webhook")
	fun receber(
		@RequestHeader("asaas-access-token", required = false) tokenRecebido: String?,
		@RequestBody payload: String
	): ResponseEntity<Void> {
		if (webhookToken.isNotBlank() && tokenRecebido != webhookToken) {
			log.warn("Webhook Asaas recebido com token inválido")
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}

		try {
			processar(payload)
		} catch (ex: Exception) {
			log.error("Falha ao processar webhook Asaas: {}", ex.message, ex)
		}

		return ResponseEntity.ok().build()
	}

	private fun processar(payload: String) {
		val evento = objectMapper.readValue(payload, AsaasWebhookPayload::class.java)

		val paymentId = evento.payment?.id
		if (paymentId != null && (evento.event == "PAYMENT_CONFIRMED" || evento.event == "PAYMENT_RECEIVED")) {
			pagamentoService.confirmarPagamentoPix(paymentId)
			return
		}

		val accountId = evento.account?.id
		val status = evento.account?.status
		if (accountId == null || status == null) {
			log.debug("Webhook Asaas ignorado (sem account.id/status nem payment reconhecido): {}", evento.event)
			return
		}

		val estabelecimento = estabelecimentoRepository.findByAsaasAccountId(accountId)
		if (estabelecimento != null) {
			estabelecimentoRepository.save(estabelecimento.copy(asaasAccountStatus = status))
			log.info("Status da subconta Asaas do estabelecimento {} atualizado para {}", estabelecimento.id, status)
			return
		}

		val profissional = profissionalRepository.findByAsaasAccountId(accountId)
		if (profissional != null) {
			profissionalRepository.save(profissional.copy(asaasAccountStatus = status))
			log.info("Status da subconta Asaas do profissional {} atualizado para {}", profissional.id, status)
			return
		}

		log.warn("Webhook Asaas recebido pra accountId {} sem estabelecimento/profissional correspondente", accountId)
	}
}
