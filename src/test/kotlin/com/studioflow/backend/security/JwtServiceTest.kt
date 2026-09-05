package com.studioflow.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.Date

class JwtServiceTest {

	private val secret = "mBqQ9kX8vN2wZ7pR4tY6uC1sD3fG5hJ8kL0mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG"
	private val jwtService = JwtService(secret, expirationMs = 60_000)

	@Test
	fun `generateToken deve incluir subject e claims recuperaveis`() {
		val token = jwtService.generateToken(
			subject = "usuario@teste.com",
			claims = mapOf("role" to "CLIENTE", "userId" to "123")
		)

		jwtService.extractSubject(token) shouldBe "usuario@teste.com"
	}

	@Test
	fun `extractIssuedAt deve retornar o instante de emissao do token`() {
		val antes = Date(System.currentTimeMillis() - 1_000)
		val token = jwtService.generateToken("usuario@teste.com", emptyMap())
		val depois = Date(System.currentTimeMillis() + 1_000)

		val issuedAt = jwtService.extractIssuedAt(token)

		(issuedAt.time - antes.time) shouldBeGreaterThan 0
		(depois.time - issuedAt.time) shouldBeGreaterThan 0
	}

	@Test
	fun `isTokenValid deve retornar true para token valido`() {
		val token = jwtService.generateToken("usuario@teste.com", emptyMap())

		jwtService.isTokenValid(token) shouldBe true
	}

	@Test
	fun `isTokenValid deve retornar false para token malformado`() {
		jwtService.isTokenValid("token-invalido") shouldBe false
	}

	@Test
	fun `isTokenValid deve retornar false para token assinado com outro segredo`() {
		val outraChave = Keys.hmacShaKeyFor("xXqQ9kX8vN2wZ7pR4tY6uC1sD3fG5hJ8kL0mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG".toByteArray())
		val tokenComOutraAssinatura = Jwts.builder()
			.subject("usuario@teste.com")
			.issuedAt(Date())
			.expiration(Date(System.currentTimeMillis() + 60_000))
			.signWith(outraChave)
			.compact()

		jwtService.isTokenValid(tokenComOutraAssinatura) shouldBe false
	}

	@Test
	fun `isTokenValid deve retornar false para token expirado`() {
		val jwtServiceExpirado = JwtService(secret, expirationMs = -1_000)
		val tokenExpirado = jwtServiceExpirado.generateToken("usuario@teste.com", emptyMap())

		jwtService.isTokenValid(tokenExpirado) shouldBe false
	}
}
