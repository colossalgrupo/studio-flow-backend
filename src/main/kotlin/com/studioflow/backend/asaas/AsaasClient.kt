package com.studioflow.backend.asaas

import com.studioflow.backend.asaas.dto.AsaasSubcontaRequest
import com.studioflow.backend.asaas.dto.AsaasSubcontaResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class AsaasClient(
	@Value("\${studioflow.asaas.api-key:}") private val apiKey: String,
	@Value("\${studioflow.asaas.ambiente:sandbox}") private val ambiente: String
) {
	private val log = LoggerFactory.getLogger(AsaasClient::class.java)

	private val restClient: RestClient by lazy {
		val baseUrl = if (ambiente == "production") "https://api.asaas.com/v3" else "https://api-sandbox.asaas.com/v3"
		RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader("access_token", apiKey)
			.build()
	}

	fun configurado(): Boolean = apiKey.isNotBlank()

	/** Retorna null se a API não estiver configurada ou a chamada falhar — nunca lança. */
	fun criarSubconta(request: AsaasSubcontaRequest): AsaasSubcontaResponse? {
		if (!configurado()) {
			log.warn("ASAAS_API_KEY não configurada — subconta não criada")
			return null
		}
		return try {
			restClient.post()
				.uri("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(AsaasSubcontaResponse::class.java)
		} catch (ex: RestClientResponseException) {
			log.error("Falha ao criar subconta Asaas: {} - {}", ex.statusCode, ex.responseBodyAsString)
			null
		} catch (ex: Exception) {
			log.error("Falha ao criar subconta Asaas: {}", ex.message, ex)
			null
		}
	}
}
