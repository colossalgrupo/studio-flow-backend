package com.studioflow.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
	@Value("\${studioflow.jwt.secret}") secret: String,
	@Value("\${studioflow.jwt.expiration-ms}") private val expirationMs: Long
) {
	private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

	fun generateToken(subject: String, claims: Map<String, Any>): String {
		val now = Date()
		val expiry = Date(now.time + expirationMs)
		return Jwts.builder()
			.subject(subject)
			.claims(claims)
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key)
			.compact()
	}

	fun extractSubject(token: String): String = parseClaims(token).subject

	fun isTokenValid(token: String): Boolean =
		try {
			parseClaims(token)
			true
		} catch (ex: Exception) {
			false
		}

	private fun parseClaims(token: String) =
		Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
