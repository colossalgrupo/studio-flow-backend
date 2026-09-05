package com.studioflow.backend.auth

import com.studioflow.backend.auth.dto.ForgotPasswordRequest
import com.studioflow.backend.auth.dto.LoginRequest
import com.studioflow.backend.auth.dto.RegisterRequest
import com.studioflow.backend.auth.dto.ResendVerificationRequest
import com.studioflow.backend.auth.dto.ResetPasswordRequest
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.email.EmailService
import com.studioflow.backend.security.JwtService
import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

class AuthServiceTest {

	private val usuarioRepository = mockk<UsuarioRepository>()
	private val passwordEncoder = mockk<PasswordEncoder>()
	private val jwtService = mockk<JwtService>()
	private val emailService = mockk<EmailService>(relaxed = true)
	private val frontendUrl = "https://studio-schedule-web.vercel.app"

	private lateinit var authService: AuthService

	@BeforeEach
	fun setUp() {
		authService = AuthService(usuarioRepository, passwordEncoder, jwtService, emailService, frontendUrl)
	}

	private fun usuarioPendente(
		id: String = "u1",
		email: String = "maria@teste.com",
		emailVerificationToken: String? = "token-verificacao"
	) = Usuario(
		id = id,
		nome = "Maria",
		email = email,
		senhaHash = "hash-antigo",
		tipoPerfil = TipoPerfil.CLIENTE,
		status = StatusUsuario.PENDING_VERIFICATION,
		emailVerificationToken = emailVerificationToken
	)

	private fun usuarioAtivo(
		id: String = "u1",
		email: String = "maria@teste.com",
		senhaHash: String = "hash-atual"
	) = Usuario(
		id = id,
		nome = "Maria",
		email = email,
		senhaHash = senhaHash,
		tipoPerfil = TipoPerfil.CLIENTE,
		status = StatusUsuario.ACTIVE
	)

	// ---------- registrar ----------

	@Test
	fun `registrar deve lancar excecao quando email ja existe`() {
		every { usuarioRepository.existsByEmail("maria@teste.com") } returns true

		val request = RegisterRequest("Maria", "maria@teste.com", "senha123", TipoPerfil.CLIENTE)

		val ex = shouldThrow<BusinessException> { authService.registrar(request) }
		ex.status shouldBe HttpStatus.CONFLICT
	}

	@Test
	fun `registrar deve criar usuario pendente e enviar e-mail de verificacao`() {
		val usuarioSalvoSlot = slot<Usuario>()
		every { usuarioRepository.existsByEmail("maria@teste.com") } returns false
		every { passwordEncoder.encode("senha123") } returns "hash-gerado"
		every { usuarioRepository.save(capture(usuarioSalvoSlot)) } answers { usuarioSalvoSlot.captured.copy(id = "u1") }

		val request = RegisterRequest("Maria", "maria@teste.com", "senha123", TipoPerfil.CLIENTE)
		val response = authService.registrar(request)

		usuarioSalvoSlot.captured.status shouldBe StatusUsuario.PENDING_VERIFICATION
		usuarioSalvoSlot.captured.senhaHash shouldBe "hash-gerado"
		(usuarioSalvoSlot.captured.emailVerificationToken != null) shouldBe true
		response.email shouldBe "maria@teste.com"

		verify {
			emailService.sendVerificationEmail(
				"maria@teste.com",
				"Maria",
				match { it.startsWith("$frontendUrl/verify-email?token=") }
			)
		}
	}

	// ---------- login ----------

	@Test
	fun `login deve lancar excecao quando usuario nao encontrado`() {
		every { usuarioRepository.findByEmail("ninguem@teste.com") } returns null

		val ex = shouldThrow<BusinessException> {
			authService.login(LoginRequest("ninguem@teste.com", "senha123"))
		}
		ex.status shouldBe HttpStatus.UNAUTHORIZED
	}

	@Test
	fun `login deve lancar excecao quando senha invalida`() {
		val usuario = usuarioAtivo()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { passwordEncoder.matches("senha-errada", usuario.senhaHash) } returns false

		val ex = shouldThrow<BusinessException> {
			authService.login(LoginRequest(usuario.email, "senha-errada"))
		}
		ex.status shouldBe HttpStatus.UNAUTHORIZED
	}

	@Test
	fun `login deve lancar excecao quando conta esta pendente de verificacao`() {
		val usuario = usuarioPendente()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { passwordEncoder.matches("senha123", usuario.senhaHash) } returns true

		val ex = shouldThrow<BusinessException> {
			authService.login(LoginRequest(usuario.email, "senha123"))
		}
		ex.status shouldBe HttpStatus.FORBIDDEN
	}

	@Test
	fun `login deve retornar token quando credenciais validas e conta ativa`() {
		val usuario = usuarioAtivo()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { passwordEncoder.matches("senha123", usuario.senhaHash) } returns true
		every { jwtService.generateToken(usuario.email, mapOf("role" to "CLIENTE", "userId" to "u1")) } returns "jwt-gerado"

		val response = authService.login(LoginRequest(usuario.email, "senha123"))

		response.token shouldBe "jwt-gerado"
		response.email shouldBe usuario.email
		response.nome shouldBe usuario.nome
		response.tipoPerfil shouldBe TipoPerfil.CLIENTE
	}

	// ---------- verificarEmail ----------

	@Test
	fun `verificarEmail deve lancar excecao quando token invalido`() {
		every { usuarioRepository.findByEmailVerificationToken("token-invalido") } returns null

		val ex = shouldThrow<BusinessException> { authService.verificarEmail("token-invalido") }
		ex.status shouldBe HttpStatus.BAD_REQUEST
	}

	@Test
	fun `verificarEmail deve ativar a conta e retornar o token`() {
		val usuario = usuarioPendente()
		val usuarioAtivadoSlot = slot<Usuario>()
		every { usuarioRepository.findByEmailVerificationToken("token-verificacao") } returns usuario
		every { usuarioRepository.save(capture(usuarioAtivadoSlot)) } answers { usuarioAtivadoSlot.captured }
		every { jwtService.generateToken(usuario.email, mapOf("role" to "CLIENTE", "userId" to "u1")) } returns "jwt-pos-verificacao"

		val response = authService.verificarEmail("token-verificacao")

		usuarioAtivadoSlot.captured.status shouldBe StatusUsuario.ACTIVE
		usuarioAtivadoSlot.captured.emailVerificationToken shouldBe null
		(usuarioAtivadoSlot.captured.emailVerifiedAt != null) shouldBe true
		response.token shouldBe "jwt-pos-verificacao"
	}

	// ---------- reenviarVerificacao ----------

	@Test
	fun `reenviarVerificacao nao faz nada quando usuario nao encontrado`() {
		every { usuarioRepository.findByEmail("ninguem@teste.com") } returns null

		authService.reenviarVerificacao(ResendVerificationRequest("ninguem@teste.com"))

		verify(exactly = 0) { usuarioRepository.save(any()) }
		verify(exactly = 0) { emailService.sendVerificationEmail(any(), any(), any()) }
	}

	@Test
	fun `reenviarVerificacao nao faz nada quando conta ja esta ativa`() {
		val usuario = usuarioAtivo()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario

		authService.reenviarVerificacao(ResendVerificationRequest(usuario.email))

		verify(exactly = 0) { usuarioRepository.save(any()) }
		verify(exactly = 0) { emailService.sendVerificationEmail(any(), any(), any()) }
	}

	@Test
	fun `reenviarVerificacao reenvia o e-mail usando o token existente quando conta pendente`() {
		val usuario = usuarioPendente(emailVerificationToken = "token-existente")
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { usuarioRepository.save(any()) } answers { firstArg() }

		authService.reenviarVerificacao(ResendVerificationRequest(usuario.email))

		verify {
			emailService.sendVerificationEmail(
				usuario.email,
				usuario.nome,
				"$frontendUrl/verify-email?token=token-existente"
			)
		}
	}

	@Test
	fun `reenviarVerificacao gera novo token quando conta pendente nao possui token`() {
		val usuario = usuarioPendente(emailVerificationToken = null)
		val salvoSlot = slot<Usuario>()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { usuarioRepository.save(capture(salvoSlot)) } answers { salvoSlot.captured }

		authService.reenviarVerificacao(ResendVerificationRequest(usuario.email))

		(salvoSlot.captured.emailVerificationToken != null) shouldBe true
		verify {
			emailService.sendVerificationEmail(usuario.email, usuario.nome, any())
		}
	}

	// ---------- esqueciSenha ----------

	@Test
	fun `esqueciSenha nao faz nada quando usuario nao encontrado`() {
		every { usuarioRepository.findByEmail("ninguem@teste.com") } returns null

		authService.esqueciSenha(ForgotPasswordRequest("ninguem@teste.com"))

		verify(exactly = 0) { usuarioRepository.save(any()) }
		verify(exactly = 0) { emailService.sendPasswordResetEmail(any(), any(), any()) }
	}

	@Test
	fun `esqueciSenha gera token com expiracao e envia e-mail quando usuario encontrado`() {
		val usuario = usuarioAtivo()
		val salvoSlot = slot<Usuario>()
		val antes = Instant.now()
		every { usuarioRepository.findByEmail(usuario.email) } returns usuario
		every { usuarioRepository.save(capture(salvoSlot)) } answers { salvoSlot.captured }

		authService.esqueciSenha(ForgotPasswordRequest(usuario.email))

		val salvo = salvoSlot.captured
		(salvo.passwordResetToken != null) shouldBe true
		(salvo.passwordResetExpiresAt!!.isAfter(antes.plusSeconds(3600))) shouldBe true
		verify {
			emailService.sendPasswordResetEmail(
				usuario.email,
				usuario.nome,
				match { it.startsWith("$frontendUrl/reset-password?token=") }
			)
		}
	}

	// ---------- validarTokenResetSenha ----------

	@Test
	fun `validarTokenResetSenha retorna false quando token nao encontrado`() {
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns null

		authService.validarTokenResetSenha("token-x") shouldBe false
	}

	@Test
	fun `validarTokenResetSenha retorna false quando token nao possui expiracao`() {
		val usuario = usuarioAtivo().copy(passwordResetToken = "token-x", passwordResetExpiresAt = null)
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario

		authService.validarTokenResetSenha("token-x") shouldBe false
	}

	@Test
	fun `validarTokenResetSenha retorna false quando token expirado`() {
		val usuario = usuarioAtivo().copy(
			passwordResetToken = "token-x",
			passwordResetExpiresAt = Instant.now().minusSeconds(60)
		)
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario

		authService.validarTokenResetSenha("token-x") shouldBe false
	}

	@Test
	fun `validarTokenResetSenha retorna true quando token valido`() {
		val usuario = usuarioAtivo().copy(
			passwordResetToken = "token-x",
			passwordResetExpiresAt = Instant.now().plusSeconds(60)
		)
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario

		authService.validarTokenResetSenha("token-x") shouldBe true
	}

	// ---------- redefinirSenha ----------

	@Test
	fun `redefinirSenha lanca excecao quando token invalido`() {
		every { usuarioRepository.findByPasswordResetToken("token-invalido") } returns null

		val ex = shouldThrow<BusinessException> {
			authService.redefinirSenha(ResetPasswordRequest("token-invalido", "novaSenha123"))
		}
		ex.status shouldBe HttpStatus.BAD_REQUEST
	}

	@Test
	fun `redefinirSenha lanca excecao quando token nao possui expiracao`() {
		val usuario = usuarioAtivo().copy(passwordResetToken = "token-x", passwordResetExpiresAt = null)
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario

		val ex = shouldThrow<BusinessException> {
			authService.redefinirSenha(ResetPasswordRequest("token-x", "novaSenha123"))
		}
		ex.status shouldBe HttpStatus.BAD_REQUEST
	}

	@Test
	fun `redefinirSenha lanca excecao quando token expirado`() {
		val usuario = usuarioAtivo().copy(
			passwordResetToken = "token-x",
			passwordResetExpiresAt = Instant.now().minusSeconds(60)
		)
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario

		val ex = shouldThrow<BusinessException> {
			authService.redefinirSenha(ResetPasswordRequest("token-x", "novaSenha123"))
		}
		ex.status shouldBe HttpStatus.BAD_REQUEST
	}

	@Test
	fun `redefinirSenha atualiza a senha e limpa o token quando valido`() {
		val usuario = usuarioAtivo().copy(
			passwordResetToken = "token-x",
			passwordResetExpiresAt = Instant.now().plusSeconds(60)
		)
		val salvoSlot = slot<Usuario>()
		every { usuarioRepository.findByPasswordResetToken("token-x") } returns usuario
		every { passwordEncoder.encode("novaSenha123") } returns "novo-hash"
		every { usuarioRepository.save(capture(salvoSlot)) } answers { salvoSlot.captured }

		authService.redefinirSenha(ResetPasswordRequest("token-x", "novaSenha123"))

		val salvo = salvoSlot.captured
		salvo.senhaHash shouldBe "novo-hash"
		salvo.passwordResetToken shouldBe null
		salvo.passwordResetExpiresAt shouldBe null
		(salvo.passwordChangedAt != null) shouldBe true
	}
}
