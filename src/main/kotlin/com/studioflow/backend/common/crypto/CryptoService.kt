package com.studioflow.backend.common.crypto

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Criptografia simétrica (AES-256-GCM) para segredos que precisam ficar no banco —
 * hoje, só a apiKey de subconta Asaas, devolvida pela Asaas uma única vez na criação
 * e por isso capturada e guardada criptografada em vez de descartada.
 */
@Service
class CryptoService(
	@Value("\${studioflow.crypto.secret-key:}") private val secretKeyBase64: String
) {
	private val secretKey by lazy { SecretKeySpec(Base64.getDecoder().decode(secretKeyBase64), "AES") }
	private val random = SecureRandom()

	fun configurado(): Boolean = secretKeyBase64.isNotBlank()

	fun encrypt(plaintext: String): String {
		check(configurado()) { "CRYPTO_SECRET_KEY não configurada" }
		val iv = ByteArray(12).also { random.nextBytes(it) }
		val cipher = Cipher.getInstance("AES/GCM/NoPadding")
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
		val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
		return Base64.getEncoder().encodeToString(iv + ciphertext)
	}

	fun decrypt(encoded: String): String {
		check(configurado()) { "CRYPTO_SECRET_KEY não configurada" }
		val combined = Base64.getDecoder().decode(encoded)
		val iv = combined.copyOfRange(0, 12)
		val ciphertext = combined.copyOfRange(12, combined.size)
		val cipher = Cipher.getInstance("AES/GCM/NoPadding")
		cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
		return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
	}
}
