package com.studioflow.backend.whatsapp.agente

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@JsonIgnoreProperties(ignoreUnknown = true)
private data class AnthropicResponse(val content: List<AnthropicContentBlock> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
private data class AnthropicContentBlock(val type: String = "", val text: String? = null)

@Service
class AnthropicClient(
	@Value("\${studioflow.anthropic.api-key:}") private val apiKey: String,
	@Value("\${studioflow.anthropic.model:claude-haiku-4-5-20251001}") private val model: String,
	private val restClient: RestClient = RestClient.create("https://api.anthropic.com")
) {
	private val log = LoggerFactory.getLogger(AnthropicClient::class.java)

	/** Retorna a resposta em texto do agente, ou null se a API não estiver configurada ou a chamada falhar. */
	fun gerarResposta(systemPrompt: String, historico: List<MensagemConversa>): String? {
		if (apiKey.isBlank()) {
			log.warn("ANTHROPIC_API_KEY não configurada — agente de WhatsApp não pode responder")
			return null
		}

		return try {
			val mensagens = historico.map { mapOf("role" to it.papel, "content" to it.texto) }
			val resposta = restClient.post()
				.uri("/v1/messages")
				.header("x-api-key", apiKey)
				.header("anthropic-version", "2023-06-01")
				.contentType(MediaType.APPLICATION_JSON)
				.body(
					mapOf(
						"model" to model,
						"max_tokens" to 500,
						"system" to systemPrompt,
						"messages" to mensagens
					)
				)
				.retrieve()
				.body(AnthropicResponse::class.java)

			resposta?.content?.firstOrNull { it.type == "text" }?.text?.trim()
		} catch (ex: Exception) {
			log.error("Falha ao chamar a Claude API: {}", ex.message, ex)
			null
		}
	}
}
