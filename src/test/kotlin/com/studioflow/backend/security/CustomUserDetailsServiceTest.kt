package com.studioflow.backend.security

import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {

	private val usuarioRepository = mockk<UsuarioRepository>()
	private val service = CustomUserDetailsService(usuarioRepository)

	@Test
	fun `loadUserByUsername deve retornar UserPrincipal quando usuario existe`() {
		val usuario = Usuario(
			id = "u1",
			nome = "Maria",
			email = "maria@teste.com",
			senhaHash = "hash",
			tipoPerfil = TipoPerfil.CLIENTE,
			status = StatusUsuario.ACTIVE
		)
		every { usuarioRepository.findByEmail("maria@teste.com") } returns usuario

		val principal = service.loadUserByUsername("maria@teste.com")

		principal.username shouldBe "maria@teste.com"
		principal.usuario shouldBe usuario
	}

	@Test
	fun `loadUserByUsername deve lancar excecao quando usuario nao existe`() {
		every { usuarioRepository.findByEmail("ninguem@teste.com") } returns null

		shouldThrow<UsernameNotFoundException> {
			service.loadUserByUsername("ninguem@teste.com")
		}
	}
}
