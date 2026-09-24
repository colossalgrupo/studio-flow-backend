package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.LocalDate

data class AsaasSplitItem(
	val walletId: String,
	val fixedValue: BigDecimal
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
