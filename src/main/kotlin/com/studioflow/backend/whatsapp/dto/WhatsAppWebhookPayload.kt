package com.studioflow.backend.whatsapp.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppWebhookPayload(
	val entry: List<WhatsAppEntry> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppEntry(
	val changes: List<WhatsAppChange> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppChange(
	val value: WhatsAppValue = WhatsAppValue(),
	val field: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppValue(
	val contacts: List<WhatsAppContact> = emptyList(),
	val messages: List<WhatsAppMessage> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppContact(
	val profile: WhatsAppProfile = WhatsAppProfile(),
	@JsonProperty("wa_id") val waId: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppProfile(
	val name: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppMessage(
	val from: String = "",
	val id: String = "",
	val type: String = "",
	val text: WhatsAppText? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppText(
	val body: String = ""
)
