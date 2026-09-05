package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.AuthResponse
import com.studioflow.backend.auth.dto.ForgotPasswordRequest
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.RegisterRequest
import com.studioflow.backend.auth.dto.RegisterResponse
import com.studioflow.backend.auth.dto.ResendVerificationRequest
import com.studioflow.backend.auth.dto.ResetPasswordRequest
import com.studioflow.backend.usuario.TipoPerfil
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthControllerTest {

	private val authService = mockk<AuthService>(relaxed = true)
	private val controller = AuthController(authService)

	@Test
	fun `registrar deve retornar 201 com o resultado do service`() {
		val request = RegisterRequest("Maria", "maria@teste.com", "senha123", TipoPerfil.CLIENTE)
		val expected = RegisterResponse("Cadastro realizado com sucesso.", "maria@teste.com")
		every { authService.registrar(request) } returns expected

		val response = controller.registrar(request)

		response.statusCode shouldBe HttpStatus.CREATED
		response.body shouldBe expected
	}

	@Test
	fun `login deve retornar 200 com o resultado do service`() {
		val request = LoginRequest("maria@teste.com", "senha123")
		val expected = AuthResponse("jwt", TipoPerfil.CLIENTE, "Maria", "maria@teste.com")
		every { authService.login(request) } returns expected

		val response = controller.login(request)

		response.statusCode shouldBe HttpStatus.OK
		response.body shouldBe expected
	}

	@Test
	fun `verifyEmail deve retornar 200 com o resultado do service`() {
		val expected = AuthResponse("jwt", TipoPerfil.CLIENTE, "Maria", "maria@teste.com")
		every { authService.verificarEmail("token-x") } returns expected

		val response = controller.verifyEmail("token-x")

		response.statusCode shouldBe HttpStatus.OK
		response.body shouldBe expected
	}

	@Test
	fun `resendVerification deve delegar ao service e retornar mensagem generica`() {
		val request = ResendVerificationRequest("maria@teste.com")

		val response = controller.resendVerification(request)

		verify { authService.reenviarVerificacao(request) }
		response.statusCode shouldBe HttpStatus.OK
		response.body?.message shouldBe "Se este e-mail estiver cadastrado e pendente de verificação, enviaremos um novo link em instantes."
	}

	@Test
	fun `forgotPassword deve delegar ao service e retornar mensagem generica`() {
		val request = ForgotPasswordRequest("maria@teste.com")

		val response = controller.forgotPassword(request)

		verify { authService.esqueciSenha(request) }
		response.statusCode shouldBe HttpStatus.OK
		response.body?.message shouldBe "Se este e-mail estiver cadastrado, enviaremos instruções de redefinição de senha em instantes."
	}

	@Test
	fun `validateResetToken deve retornar valid true quando token valido`() {
		every { authService.validarTokenResetSenha("token-x") } returns true

		val response = controller.validateResetToken("token-x")

		response.statusCode shouldBe HttpStatus.OK
		response.body?.valid shouldBe true
	}

	@Test
	fun `validateResetToken deve retornar valid false quando token invalido`() {
		every { authService.validarTokenResetSenha("token-invalido") } returns false

		val response = controller.validateResetToken("token-invalido")

		response.body?.valid shouldBe false
	}

	@Test
	fun `resetPassword deve delegar ao service e retornar mensagem de sucesso`() {
		val request = ResetPasswordRequest("token-x", "novaSenha123")

		val response = controller.resetPassword(request)

		verify { authService.redefinirSenha(request) }
		response.statusCode shouldBe HttpStatus.OK
		response.body?.message shouldBe "Senha redefinida com sucesso."
	}
}
