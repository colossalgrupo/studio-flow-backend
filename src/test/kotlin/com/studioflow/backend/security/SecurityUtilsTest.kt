package com.studioflow.backend.security

import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class SecurityUtilsTest {

	@AfterEach
	fun limparContexto() {
		SecurityContextHolder.clearContext()
	}

	private fun autenticarComo(usuario: Usuario) {
		val principal = UserPrincipal(usuario)
		SecurityContextHolder.getContext().authentication =
			UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
	}

	@Test
	fun `currentUser deve retornar o principal autenticado`() {
		val usuario = Usuario(
			id = "u1",
			nome = "Maria",
			email = "maria@teste.com",
			senhaHash = "hash",
			tipoPerfil = TipoPerfil.CLIENTE,
			status = StatusUsuario.ACTIVE
		)
		autenticarComo(usuario)

		SecurityUtils.currentUser().usuario shouldBe usuario
	}

	@Test
	fun `currentUserId deve retornar o id do usuario autenticado`() {
		val usuario = Usuario(
			id = "u2",
			nome = "João",
			email = "joao@teste.com",
			senhaHash = "hash",
			tipoPerfil = TipoPerfil.EMPREENDEDOR,
			status = StatusUsuario.ACTIVE
		)
		autenticarComo(usuario)

		SecurityUtils.currentUserId() shouldBe "u2"
	}
}
