package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AsaasSubcontaRequest(
	val name: String,
	val email: String,
	val cpfCnpj: String,
	val birthDate: LocalDate? = null,
	val companyType: String? = null,
	val incomeValue: BigDecimal,
	val address: String,
	val addressNumber: String,
	val province: String,
	val postalCode: String,
	val mobilePhone: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasSubcontaResponse(
	val id: String? = null,
	val walletId: String? = null,
	val apiKey: String? = null
)
