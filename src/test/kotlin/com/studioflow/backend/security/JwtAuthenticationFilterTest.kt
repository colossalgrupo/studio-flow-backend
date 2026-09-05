package com.studioflow.backend.security

import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant

class JwtAuthenticationFilterTest {

	private val secret = "mBqQ9kX8vN2wZ7pR4tY6uC1sD3fG5hJ8kL0mN2pQ4rS6tU8vW0xY2zA4bC6dE8fG"
	private val jwtService = JwtService(secret, expirationMs = 60_000)
	private val userDetailsService = mockk<CustomUserDetailsService>()
	private val filter = JwtAuthenticationFilter(jwtService, userDetailsService)

	@AfterEach
	fun limparContexto() {
		SecurityContextHolder.clearContext()
	}

	private fun usuario(passwordChangedAt: Instant? = null) = Usuario(
		id = "u1",
		nome = "Maria",
		email = "maria@teste.com",
		senhaHash = "hash",
		tipoPerfil = TipoPerfil.CLIENTE,
		status = StatusUsuario.ACTIVE,
		passwordChangedAt = passwordChangedAt
	)

	@Test
	fun `sem header Authorization deve seguir a cadeia sem autenticar`() {
		val request = MockHttpServletRequest()
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		SecurityContextHolder.getContext().authentication shouldBe null
	}

	@Test
	fun `header sem prefixo Bearer deve seguir a cadeia sem autenticar`() {
		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Basic algumacoisa")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		SecurityContextHolder.getContext().authentication shouldBe null
	}

	@Test
	fun `token invalido nao deve autenticar`() {
		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Bearer token-invalido")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		SecurityContextHolder.getContext().authentication shouldBe null
		verify(exactly = 0) { userDetailsService.loadUserByUsername(any()) }
	}

	@Test
	fun `token valido sem passwordChangedAt deve autenticar`() {
		val token = jwtService.generateToken("maria@teste.com", emptyMap())
		every { userDetailsService.loadUserByUsername("maria@teste.com") } returns UserPrincipal(usuario())

		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Bearer $token")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		val auth = SecurityContextHolder.getContext().authentication
		auth shouldNotBe null
		(auth is UsernamePasswordAuthenticationToken) shouldBe true
	}

	@Test
	fun `token valido com passwordChangedAt anterior ao iat deve autenticar`() {
		val token = jwtService.generateToken("maria@teste.com", emptyMap())
		every { userDetailsService.loadUserByUsername("maria@teste.com") } returns
			UserPrincipal(usuario(passwordChangedAt = Instant.now().minusSeconds(60)))

		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Bearer $token")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		SecurityContextHolder.getContext().authentication shouldNotBe null
	}

	@Test
	fun `token valido com passwordChangedAt posterior ao iat nao deve autenticar`() {
		val token = jwtService.generateToken("maria@teste.com", emptyMap())
		every { userDetailsService.loadUserByUsername("maria@teste.com") } returns
			UserPrincipal(usuario(passwordChangedAt = Instant.now().plusSeconds(60)))

		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Bearer $token")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		SecurityContextHolder.getContext().authentication shouldBe null
	}

	@Test
	fun `nao deve sobrescrever autenticacao ja presente no contexto`() {
		val principalExistente = UserPrincipal(usuario())
		SecurityContextHolder.getContext().authentication =
			UsernamePasswordAuthenticationToken(principalExistente, null, principalExistente.authorities)

		val token = jwtService.generateToken("maria@teste.com", emptyMap())
		val request = MockHttpServletRequest()
		request.addHeader("Authorization", "Bearer $token")
		val response = MockHttpServletResponse()
		val chain = MockFilterChain()

		filter.doFilter(request, response, chain)

		verify(exactly = 0) { userDetailsService.loadUserByUsername(any()) }
	}
}
