package com.studioflow.backend.asaas.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AsaasClienteRequest(
	val name: String,
	val email: String,
	val cpfCnpj: String,
	val mobilePhone: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsaasClienteResponse(val id: String? = null)
