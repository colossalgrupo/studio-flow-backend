package com.studioflow.backend.asaas

import com.studioflow.backend.asaas.dto.AsaasAssinaturaRequest
import com.studioflow.backend.asaas.dto.AsaasAssinaturaResponse
import com.studioflow.backend.asaas.dto.AsaasClienteRequest
import com.studioflow.backend.asaas.dto.AsaasClienteResponse
import com.studioflow.backend.asaas.dto.AsaasCobrancaRequest
import com.studioflow.backend.asaas.dto.AsaasCobrancaResponse
import com.studioflow.backend.asaas.dto.AsaasPixQrCodeResponse
import com.studioflow.backend.asaas.dto.AsaasSubcontaRequest
import com.studioflow.backend.asaas.dto.AsaasSubcontaResponse
import com.studioflow.backend.asaas.dto.AsaasTransferenciaRequest
import com.studioflow.backend.asaas.dto.AsaasTransferenciaResponse
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

	private val baseUrl by lazy {
		if (ambiente == "production") "https://api.asaas.com/v3" else "https://api-sandbox.asaas.com/v3"
	}

	private val restClient: RestClient by lazy { restClientCom(apiKey) }

	private fun restClientCom(chave: String): RestClient =
		RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader("access_token", chave)
			.build()

	fun configurado(): Boolean = apiKey.isNotBlank()

	/** Retorna null se a API não estiver configurada ou a chamada falhar — nunca lança. */
	fun criarSubconta(request: AsaasSubcontaRequest): AsaasSubcontaResponse? {
		if (!configurado()) {
			log.warn("ASAAS_API_KEY não configurada — subconta não criada")
			return null
		}
		return chamar("criar subconta") {
			restClient.post().uri("/accounts").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().body(AsaasSubcontaResponse::class.java)
		}
	}

	fun criarCliente(request: AsaasClienteRequest): AsaasClienteResponse? {
		if (!configurado()) {
			log.warn("ASAAS_API_KEY não configurada — cliente não criado")
			return null
		}
		return chamar("criar cliente") {
			restClient.post().uri("/customers").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().body(AsaasClienteResponse::class.java)
		}
	}

	fun criarCobranca(request: AsaasCobrancaRequest): AsaasCobrancaResponse? {
		if (!configurado()) {
			log.warn("ASAAS_API_KEY não configurada — cobrança não criada")
			return null
		}
		return chamar("criar cobrança") {
			restClient.post().uri("/payments").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().body(AsaasCobrancaResponse::class.java)
		}
	}

	fun criarAssinatura(request: AsaasAssinaturaRequest): AsaasAssinaturaResponse? {
		if (!configurado()) {
			log.warn("ASAAS_API_KEY não configurada — assinatura não criada")
			return null
		}
		return chamar("criar assinatura") {
			restClient.post().uri("/subscriptions").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().body(AsaasAssinaturaResponse::class.java)
		}
	}

	fun obterQrCodePix(paymentId: String): AsaasPixQrCodeResponse? {
		if (!configurado()) return null
		return chamar("obter QR code Pix") {
			restClient.get().uri("/payments/{id}/pixQrCode", paymentId)
				.retrieve().body(AsaasPixQrCodeResponse::class.java)
		}
	}

	/** Transferência disparada com a apiKey da PRÓPRIA subconta (não a chave mestra). */
	fun criarTransferencia(apiKeySubconta: String, request: AsaasTransferenciaRequest): AsaasTransferenciaResponse? {
		return chamar("criar transferência") {
			restClientCom(apiKeySubconta).post().uri("/transfers").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().body(AsaasTransferenciaResponse::class.java)
		}
	}

	private fun <T> chamar(descricao: String, acao: () -> T?): T? = try {
		acao()
	} catch (ex: RestClientResponseException) {
		log.error("Falha ao {} na Asaas: {} - {}", descricao, ex.statusCode, ex.responseBodyAsString)
		null
	} catch (ex: Exception) {
		log.error("Falha ao {} na Asaas: {}", descricao, ex.message, ex)
		null
	}
}
