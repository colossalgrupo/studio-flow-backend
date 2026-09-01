package com.studioflow.backend.common.exception

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ErrorResponse(
	val timestamp: Instant = Instant.now(),
	val status: Int,
	val error: String,
	val message: String?,
	val fieldErrors: Map<String, String>? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException::class)
	fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(ex.status).body(
			ErrorResponse(status = ex.status.value(), error = ex.status.reasonPhrase, message = ex.message)
		)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
		val fieldErrors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "inválido") }
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			ErrorResponse(
				status = HttpStatus.BAD_REQUEST.value(),
				error = HttpStatus.BAD_REQUEST.reasonPhrase,
				message = "Erro de validação",
				fieldErrors = fieldErrors
			)
		)
	}

	@ExceptionHandler(DuplicateKeyException::class)
	fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.CONFLICT).body(
			ErrorResponse(
				status = HttpStatus.CONFLICT.value(),
				error = HttpStatus.CONFLICT.reasonPhrase,
				message = "Registro duplicado ou conflito de horário"
			)
		)

	@ExceptionHandler(AccessDeniedException::class)
	fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.FORBIDDEN).body(
			ErrorResponse(status = HttpStatus.FORBIDDEN.value(), error = HttpStatus.FORBIDDEN.reasonPhrase, message = "Acesso negado")
		)

	@ExceptionHandler(BadCredentialsException::class)
	fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
			ErrorResponse(status = HttpStatus.UNAUTHORIZED.value(), error = HttpStatus.UNAUTHORIZED.reasonPhrase, message = "Credenciais inválidas")
		)

	@ExceptionHandler(NoSuchElementException::class)
	fun handleNotFoundException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(
			ErrorResponse(status = HttpStatus.NOT_FOUND.value(), error = HttpStatus.NOT_FOUND.reasonPhrase, message = ex.message)
		)
}
