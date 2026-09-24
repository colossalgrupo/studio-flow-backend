package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AsaasAssinaturaRequest(
	val customer: String,
	val billingType: String = "PIX",
	val value: BigDecimal,
	val cycle: String = "MONTHLY",
	val nextDueDate: LocalDate,
	val externalReference: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasAssinaturaResponse(
	val id: String? = null,
	val status: String? = null
)
