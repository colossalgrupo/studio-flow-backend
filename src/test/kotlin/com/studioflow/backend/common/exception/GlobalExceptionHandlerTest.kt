package com.studioflow.backend.common.exception

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.core.MethodParameter

class GlobalExceptionHandlerTest {

	private val handler = GlobalExceptionHandler()

	@Suppress("UNUSED_PARAMETER")
	private fun metodoFalso(valor: String) = Unit

	@Test
	fun `handleBusinessException deve retornar o status e a mensagem da excecao`() {
		val ex = BusinessException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail")

		val response = handler.handleBusinessException(ex)

		response.statusCode shouldBe HttpStatus.CONFLICT
		response.body?.status shouldBe HttpStatus.CONFLICT.value()
		response.body?.error shouldBe HttpStatus.CONFLICT.reasonPhrase
		response.body?.message shouldBe "Já existe um usuário cadastrado com este e-mail"
	}

	@Test
	fun `handleValidationException deve retornar 400 com os erros de campo`() {
		val bindingResult = BeanPropertyBindingResult(Any(), "request")
		bindingResult.addError(FieldError("request", "email", "E-mail inválido"))
		val methodParameter = MethodParameter(this::class.java.getDeclaredMethod("metodoFalso", String::class.java), 0)
		val ex = MethodArgumentNotValidException(methodParameter, bindingResult)

		val response = handler.handleValidationException(ex)

		response.statusCode shouldBe HttpStatus.BAD_REQUEST
		response.body?.fieldErrors?.get("email") shouldBe "E-mail inválido"
		response.body?.message shouldBe "Erro de validação"
	}

	@Test
	fun `handleDuplicateKeyException deve retornar 409`() {
		val response = handler.handleDuplicateKeyException(DuplicateKeyException("duplicado"))

		response.statusCode shouldBe HttpStatus.CONFLICT
		response.body?.message shouldBe "Registro duplicado ou conflito de horário"
	}

	@Test
	fun `handleAccessDeniedException deve retornar 403`() {
		val response = handler.handleAccessDeniedException(AccessDeniedException("negado"))

		response.statusCode shouldBe HttpStatus.FORBIDDEN
		response.body?.message shouldBe "Acesso negado"
	}

	@Test
	fun `handleBadCredentialsException deve retornar 401`() {
		val response = handler.handleBadCredentialsException(BadCredentialsException("credenciais invalidas"))

		response.statusCode shouldBe HttpStatus.UNAUTHORIZED
		response.body?.message shouldBe "Credenciais inválidas"
	}

	@Test
	fun `handleNotFoundException deve retornar 404 com a mensagem da excecao`() {
		val response = handler.handleNotFoundException(NoSuchElementException("não encontrado"))

		response.statusCode shouldBe HttpStatus.NOT_FOUND
		response.body?.message shouldBe "não encontrado"
	}
}
