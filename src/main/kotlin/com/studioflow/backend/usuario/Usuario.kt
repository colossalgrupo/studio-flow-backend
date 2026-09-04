package com.studioflow.backend.usuario

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "usuarios")
data class Usuario(
	@Id val id: String? = null,
	val nome: String,
	@Indexed(unique = true) val email: String,
	val senhaHash: String,
	val tipoPerfil: TipoPerfil,
	val status: StatusUsuario = StatusUsuario.PENDING_VERIFICATION,
	@Indexed val emailVerificationToken: String? = null,
	val emailVerifiedAt: Instant? = null,
	@Indexed val passwordResetToken: String? = null,
	val passwordResetExpiresAt: Instant? = null,
	val passwordChangedAt: Instant? = null
)
