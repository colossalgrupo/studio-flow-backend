package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.AuthResponse
import com.studioflow.backend.auth.dto.ForgotPasswordRequest
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.MessageResponse
import com.studioflow.backend.auth.dto.RegisterRequest
import com.studioflow.backend.auth.dto.RegisterResponse
import com.studioflow.backend.auth.dto.ResendVerificationRequest
import com.studioflow.backend.auth.dto.ResetPasswordRequest
import com.studioflow.backend.auth.dto.TokenValidResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService
) {

	@PostMapping("/registrar")
	fun registrar(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> =
		ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request))

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
		ResponseEntity.ok(authService.login(request))

	@GetMapping("/verify-email")
	fun verifyEmail(@RequestParam token: String): ResponseEntity<AuthResponse> =
		ResponseEntity.ok(authService.verificarEmail(token))

	@PostMapping("/resend-verification")
	fun resendVerification(@Valid @RequestBody request: ResendVerificationRequest): ResponseEntity<MessageResponse> {
		authService.reenviarVerificacao(request)
		return ResponseEntity.ok(
			MessageResponse("Se este e-mail estiver cadastrado e pendente de verificação, enviaremos um novo link em instantes.")
		)
	}

	@PostMapping("/forgot-password")
	fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
		authService.esqueciSenha(request)
		return ResponseEntity.ok(
			MessageResponse("Se este e-mail estiver cadastrado, enviaremos instruções de redefinição de senha em instantes.")
		)
	}

	@GetMapping("/reset-password/validate")
	fun validateResetToken(@RequestParam token: String): ResponseEntity<TokenValidResponse> =
		ResponseEntity.ok(TokenValidResponse(authService.validarTokenResetSenha(token)))

	@PostMapping("/reset-password")
	fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
		authService.redefinirSenha(request)
		return ResponseEntity.ok(MessageResponse("Senha redefinida com sucesso."))
	}
}
