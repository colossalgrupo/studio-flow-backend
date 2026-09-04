package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.AuthResponse
import com.studioflow.backend.auth.dto.ForgotPasswordRequest
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.RegisterRequest
import com.studioflow.backend.auth.dto.RegisterResponse
import com.studioflow.backend.auth.dto.ResendVerificationRequest
import com.studioflow.backend.auth.dto.ResetPasswordRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.email.EmailService
import com.studioflow.backend.security.JwtService
import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
	private val usuarioRepository: UsuarioRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService,
	private val emailService: EmailService,
	@Value("\${studioflow.frontend-url}") private val frontendUrl: String
) {

	fun registrar(request: RegisterRequest): RegisterResponse {
		if (usuarioRepository.existsByEmail(request.email)) {
			throw BusinessException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail")
		}

		val verificationToken = gerarToken()
		val usuario = usuarioRepository.save(
			Usuario(
				nome = request.nome,
				email = request.email,
				senhaHash = passwordEncoder.encode(request.senha),
				tipoPerfil = request.tipoPerfil,
				status = StatusUsuario.PENDING_VERIFICATION,
				emailVerificationToken = verificationToken
			)
		)

		enviarEmailVerificacao(usuario, verificationToken)

		return RegisterResponse(
			message = "Cadastro realizado com sucesso. Verifique seu e-mail para ativar sua conta.",
			email = usuario.email
		)
	}

	fun login(request: LoginRequest): AuthResponse {
		val usuario = usuarioRepository.findByEmail(request.email)
			?: throw BusinessException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")

		if (!passwordEncoder.matches(request.senha, usuario.senhaHash)) {
			throw BusinessException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
		}

		if (usuario.status == StatusUsuario.PENDING_VERIFICATION) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Verifique seu e-mail antes de fazer login")
		}

		return buildResponse(usuario)
	}

	fun verificarEmail(token: String): AuthResponse {
		val usuario = usuarioRepository.findByEmailVerificationToken(token)
			?: throw BusinessException(HttpStatus.BAD_REQUEST, "Token de verificação inválido")

		val usuarioAtivado = usuarioRepository.save(
			usuario.copy(
				status = StatusUsuario.ACTIVE,
				emailVerifiedAt = Instant.now(),
				emailVerificationToken = null
			)
		)

		return buildResponse(usuarioAtivado)
	}

	fun reenviarVerificacao(request: ResendVerificationRequest) {
		val usuario = usuarioRepository.findByEmail(request.email) ?: return
		if (usuario.status != StatusUsuario.PENDING_VERIFICATION) return

		val token = usuario.emailVerificationToken ?: gerarToken()
		val usuarioAtualizado = usuarioRepository.save(usuario.copy(emailVerificationToken = token))
		enviarEmailVerificacao(usuarioAtualizado, token)
	}

	fun esqueciSenha(request: ForgotPasswordRequest) {
		val usuario = usuarioRepository.findByEmail(request.email) ?: return

		val token = gerarToken()
		val usuarioAtualizado = usuarioRepository.save(
			usuario.copy(
				passwordResetToken = token,
				passwordResetExpiresAt = Instant.now().plus(Duration.ofHours(2))
			)
		)

		val resetUrl = "$frontendUrl/reset-password?token=$token"
		emailService.sendPasswordResetEmail(usuarioAtualizado.email, usuarioAtualizado.nome, resetUrl)
	}

	fun validarTokenResetSenha(token: String): Boolean {
		val usuario = usuarioRepository.findByPasswordResetToken(token) ?: return false
		val expiraEm = usuario.passwordResetExpiresAt ?: return false
		return expiraEm.isAfter(Instant.now())
	}

	fun redefinirSenha(request: ResetPasswordRequest) {
		val usuario = usuarioRepository.findByPasswordResetToken(request.token)
			?: throw BusinessException(HttpStatus.BAD_REQUEST, "Token de redefinição inválido")

		val expiraEm = usuario.passwordResetExpiresAt
		if (expiraEm == null || expiraEm.isBefore(Instant.now())) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "Token de redefinição expirado")
		}

		usuarioRepository.save(
			usuario.copy(
				senhaHash = passwordEncoder.encode(request.newPassword),
				passwordResetToken = null,
				passwordResetExpiresAt = null,
				passwordChangedAt = Instant.now()
			)
		)
	}

	private fun enviarEmailVerificacao(usuario: Usuario, token: String) {
		val verificationUrl = "$frontendUrl/verify-email?token=$token"
		emailService.sendVerificationEmail(usuario.email, usuario.nome, verificationUrl)
	}

	private fun gerarToken(): String = UUID.randomUUID().toString().replace("-", "")

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
