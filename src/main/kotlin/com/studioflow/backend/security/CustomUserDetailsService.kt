package com.studioflow.backend.security

import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
	private val usuarioRepository: UsuarioRepository
) : UserDetailsService {

	override fun loadUserByUsername(username: String): UserPrincipal {
		val usuario = usuarioRepository.findByEmail(username)
			?: throw UsernameNotFoundException("Usuário não encontrado: $username")
		return UserPrincipal(usuario)
	}
}
