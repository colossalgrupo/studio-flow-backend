package com.studioflow.backend.security

import com.studioflow.backend.usuario.Usuario
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(val usuario: Usuario) : UserDetails {

	val id: String get() = usuario.id!!

	override fun getAuthorities(): Collection<GrantedAuthority> =
		listOf(SimpleGrantedAuthority("ROLE_${usuario.tipoPerfil.name}"))

	override fun getPassword(): String = usuario.senhaHash
	override fun getUsername(): String = usuario.email
	override fun isAccountNonExpired(): Boolean = true
	override fun isAccountNonLocked(): Boolean = true
	override fun isCredentialsNonExpired(): Boolean = true
	override fun isEnabled(): Boolean = true
}
