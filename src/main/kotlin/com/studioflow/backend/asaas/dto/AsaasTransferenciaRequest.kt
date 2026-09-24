package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

data class AsaasTransferenciaRequest(
	val value: BigDecimal,
	val pixAddressKey: String,
	val pixAddressKeyType: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasTransferenciaResponse(
	val id: String? = null,
	val status: String? = null
)
