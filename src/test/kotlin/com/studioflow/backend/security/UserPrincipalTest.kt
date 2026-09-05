package com.studioflow.backend.security

import com.studioflow.backend.usuario.StatusUsuario
import com.studioflow.backend.usuario.TipoPerfil
import com.studioflow.backend.usuario.Usuario
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserPrincipalTest {

	private val usuario = Usuario(
		id = "u1",
		nome = "Maria",
		email = "maria@teste.com",
		senhaHash = "hash",
		tipoPerfil = TipoPerfil.CLIENTE,
		status = StatusUsuario.ACTIVE
	)

	private val principal = UserPrincipal(usuario)

	@Test
	fun `id deve retornar o id do usuario`() {
		principal.id shouldBe "u1"
	}

	@Test
	fun `getAuthorities deve retornar a role prefixada`() {
		principal.authorities shouldContainExactly listOf(SimpleGrantedAuthority("ROLE_CLIENTE"))
	}

	@Test
	fun `getPassword deve retornar o hash da senha`() {
		principal.password shouldBe "hash"
	}

	@Test
	fun `getUsername deve retornar o email`() {
		principal.username shouldBe "maria@teste.com"
	}

	@Test
	fun `flags de conta devem ser sempre verdadeiras`() {
		principal.isAccountNonExpired shouldBe true
		principal.isAccountNonLocked shouldBe true
		principal.isCredentialsNonExpired shouldBe true
		principal.isEnabled shouldBe true
	}
}
