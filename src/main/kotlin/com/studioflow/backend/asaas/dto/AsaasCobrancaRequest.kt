package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Sempre percentual, nunca valor fixo — a Asaas desconta a própria taxa da cobrança
 * (ex.: R$0,99 no Pix) ANTES de aplicar o split, e o percentual é calculado sobre esse
 * valor líquido automaticamente. Testado em sandbox: com fixedValue, se a soma dos splits
 * passar do valor líquido (o que acontece fácil quando a taxa da plataforma é pequena),
 * a cobrança é rejeitada. Com percentual isso nunca acontece, porque a soma dos
 * percentuais dos splits é sempre menor que 100%.
 */
data class AsaasSplitItem(
	val walletId: String,
	val percentualValue: BigDecimal
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AsaasCobrancaRequest(
	val customer: String,
	val billingType: String = "PIX",
	val value: BigDecimal,
	val dueDate: LocalDate,
	val externalReference: String,
	val split: List<AsaasSplitItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasCobrancaResponse(
	val id: String? = null,
	val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasPixQrCodeResponse(
	val encodedImage: String? = null,
	val payload: String? = null,
	val expirationDate: String? = null
)
