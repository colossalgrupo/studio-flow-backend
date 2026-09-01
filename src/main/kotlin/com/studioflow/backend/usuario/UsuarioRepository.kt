package com.studioflow.backend.usuario

import org.springframework.data.mongodb.repository.MongoRepository

interface UsuarioRepository : MongoRepository<Usuario, String> {
	fun findByEmail(email: String): Usuario?
	fun existsByEmail(email: String): Boolean
}
