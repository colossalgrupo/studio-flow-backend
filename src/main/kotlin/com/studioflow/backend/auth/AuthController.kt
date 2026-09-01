package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.AuthResponse
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.RegisterRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val authService: AuthService
) {

	@PostMapping("/registrar")
	fun registrar(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
		ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request))

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
		ResponseEntity.ok(authService.login(request))
}
