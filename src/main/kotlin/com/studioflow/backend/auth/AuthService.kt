package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.AuthResponse
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.RegisterRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.security.JwtService
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
	private val usuarioRepository: UsuarioRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService
) {

	fun registrar(request: RegisterRequest): AuthResponse {
		if (usuarioRepository.existsByEmail(request.email)) {
			throw BusinessException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail")
		}

		val usuario = usuarioRepository.save(
			Usuario(
				nome = request.nome,
				email = request.email,
				senhaHash = passwordEncoder.encode(request.senha),
				tipoPerfil = request.tipoPerfil
			)
		)

		return buildResponse(usuario)
	}

	fun login(request: LoginRequest): AuthResponse {
		val usuario = usuarioRepository.findByEmail(request.email)
			?: throw BusinessException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")

		if (!passwordEncoder.matches(request.senha, usuario.senhaHash)) {
			throw BusinessException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
		}

		return buildResponse(usuario)
	}

	private fun buildResponse(usuario: Usuario): AuthResponse {
		val token = jwtService.generateToken(
			subject = usuario.email,
			claims = mapOf("role" to usuario.tipoPerfil.name, "userId" to usuario.id!!)
		)
		return AuthResponse(
			token = token,
			tipoPerfil = usuario.tipoPerfil,
			nome = usuario.nome,
			email = usuario.email
		)
	}
}
