package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Payload de webhook de eventos de conta da Asaas (ACCOUNT_STATUS_*). Só os campos
 * que usamos pra atualizar o status local — o resto do payload é ignorado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayload(
	val event: String? = null,
	val account: AsaasWebhookAccount? = null,
	val payment: AsaasWebhookPayment? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookAccount(
	val id: String? = null,
	val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasWebhookPayment(
	val id: String? = null,
	val status: String? = null
)
