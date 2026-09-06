package com.studioflow.backend.email

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient

class EmailServiceTest {

	private val restClient = mockk<RestClient>()
	private val requestBodyUriSpec = mockk<RestClient.RequestBodyUriSpec>()
	private val requestBodySpec = mockk<RestClient.RequestBodySpec>()
	private val responseSpec = mockk<RestClient.ResponseSpec>()

	private fun emailService(apiKey: String) =
		EmailService(apiKey = apiKey, from = "Studio Schedulle <naoresponda@studioschedulle.com.br>", restClient = restClient)

	@Test
	fun `construtor com RestClient padrao nao deve lancar excecao`() {
		EmailService(apiKey = "", from = "Studio Schedulle <naoresponda@studioschedulle.com.br>")
	}

	@Test
	fun `nao chama a API do Resend quando a api-key esta vazia`() {
		val service = emailService(apiKey = "")

		service.sendVerificationEmail("cliente@teste.com", "Maria", "https://app/verify-email?token=abc")
		service.sendPasswordResetEmail("cliente@teste.com", "Maria", "https://app/reset-password?token=abc")

		verify(exactly = 0) { restClient.post() }
	}

	@Test
	fun `sendVerificationEmail deve chamar a API do Resend quando a api-key esta configurada`() {
		val bodySlot = slot<Map<String, Any>>()
		every { restClient.post() } returns requestBodyUriSpec
		every { requestBodyUriSpec.uri("/emails") } returns requestBodySpec
		every { requestBodySpec.header(any(), *anyVararg()) } returns requestBodySpec
        every { requestBodySpec.contentType(any()) } returns requestBodySpec
		every { requestBodySpec.body(capture(bodySlot)) } returns requestBodySpec
		every { requestBodySpec.retrieve() } returns responseSpec
		every { responseSpec.toBodilessEntity() } returns ResponseEntity.ok().build()

		val service = emailService(apiKey = "re_teste_123")
		service.sendVerificationEmail("cliente@teste.com", "Maria", "https://app/verify-email?token=abc")

		verify { requestBodySpec.header("Authorization", "Bearer re_teste_123") }
		bodySlot.captured["to"] shouldBe listOf("cliente@teste.com")
		bodySlot.captured["subject"] shouldBe "Confirme seu e-mail — Studio Schedulle"
		(bodySlot.captured["html"] as String) shouldContain "https://app/verify-email?token=abc"
		(bodySlot.captured["html"] as String) shouldContain "Maria"
	}

	@Test
	fun `sendPasswordResetEmail deve chamar a API do Resend quando a api-key esta configurada`() {
		val bodySlot = slot<Map<String, Any>>()
		every { restClient.post() } returns requestBodyUriSpec
		every { requestBodyUriSpec.uri("/emails") } returns requestBodySpec
		every { requestBodySpec.header(any(), *anyVararg()) } returns requestBodySpec
		every { requestBodySpec.contentType(any()) } returns requestBodySpec
		every { requestBodySpec.body(capture(bodySlot)) } returns requestBodySpec
		every { requestBodySpec.retrieve() } returns responseSpec
		every { responseSpec.toBodilessEntity() } returns ResponseEntity.ok().build()

		val service = emailService(apiKey = "re_teste_123")
		service.sendPasswordResetEmail("cliente@teste.com", "Maria", "https://app/reset-password?token=xyz")

		bodySlot.captured["subject"] shouldBe "Redefinição de senha — Studio Schedulle"
		(bodySlot.captured["html"] as String) shouldContain "https://app/reset-password?token=xyz"
	}

	@Test
	fun `falha ao chamar a API do Resend nao deve propagar excecao`() {
		every { restClient.post() } throws RuntimeException("Resend indisponível")

		val service = emailService(apiKey = "re_teste_123")

		service.sendVerificationEmail("cliente@teste.com", "Maria", "https://app/verify-email?token=abc")
	}
}
