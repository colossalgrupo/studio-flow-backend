package com.studioflow.backend.email

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class EmailService(
	@Value("\${studioflow.resend.api-key:}") private val apiKey: String,
	@Value("\${studioflow.email.from}") private val from: String
) {
	private val log = LoggerFactory.getLogger(EmailService::class.java)
	private val restClient = RestClient.create("https://api.resend.com")

	fun sendVerificationEmail(to: String, nome: String, verificationUrl: String) {
		send(
			to = to,
			subject = "Confirme seu e-mail — Studio Schedule",
			html = layout(
				titulo = "Confirme seu e-mail",
				nome = nome,
				corpo = "Falta pouco para começar a usar o Studio Schedule. Clique no botão abaixo para confirmar seu e-mail e ativar sua conta.",
				botaoTexto = "Confirmar e-mail",
				botaoUrl = verificationUrl
			)
		)
	}

	fun sendPasswordResetEmail(to: String, nome: String, resetUrl: String) {
		send(
			to = to,
			subject = "Redefinição de senha — Studio Schedule",
			html = layout(
				titulo = "Redefinir sua senha",
				nome = nome,
				corpo = "Recebemos um pedido para redefinir a senha da sua conta no Studio Schedule. Se foi você, clique no botão abaixo. O link expira em 2 horas. Se não foi você, ignore este e-mail.",
				botaoTexto = "Redefinir senha",
				botaoUrl = resetUrl
			)
		)
	}

	private fun send(to: String, subject: String, html: String) {
		if (apiKey.isBlank()) {
			log.warn("RESEND_API_KEY não configurada — e-mail para {} não foi enviado (assunto: {})", to, subject)
			return
		}
		try {
			restClient.post()
				.uri("/emails")
				.header("Authorization", "Bearer $apiKey")
				.contentType(MediaType.APPLICATION_JSON)
				.body(mapOf("from" to from, "to" to listOf(to), "subject" to subject, "html" to html))
				.retrieve()
				.toBodilessEntity()
		} catch (ex: Exception) {
			log.error("Falha ao enviar e-mail para {} (assunto: {}): {}", to, subject, ex.message, ex)
		}
	}

	private fun layout(titulo: String, nome: String, corpo: String, botaoTexto: String, botaoUrl: String): String = """
		<!doctype html>
		<html lang="pt-BR">
			<body style="margin:0;padding:0;background-color:#F5F6F3;font-family:Arial,Helvetica,sans-serif;">
				<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#F5F6F3;padding:32px 0;">
					<tr>
						<td align="center">
							<table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#FFFFFF;border-radius:12px;overflow:hidden;">
								<tr>
									<td style="background-color:#0F6B5C;padding:24px 32px;">
										<span style="color:#FFFFFF;font-size:20px;font-weight:bold;">Studio Schedule</span>
									</td>
								</tr>
								<tr>
									<td style="padding:32px;">
										<h1 style="color:#1A1A1A;font-size:20px;margin:0 0 16px;">$titulo</h1>
										<p style="color:#4A4A4A;font-size:15px;line-height:1.5;margin:0 0 8px;">Olá, $nome!</p>
										<p style="color:#4A4A4A;font-size:15px;line-height:1.5;margin:0 0 24px;">$corpo</p>
										<table role="presentation" cellpadding="0" cellspacing="0">
											<tr>
												<td style="background-color:#0F6B5C;border-radius:8px;">
													<a href="$botaoUrl" style="display:inline-block;padding:12px 24px;color:#FFFFFF;text-decoration:none;font-size:15px;font-weight:bold;">$botaoTexto</a>
												</td>
											</tr>
										</table>
										<p style="color:#8A8A8A;font-size:13px;line-height:1.5;margin:24px 0 0;">Se o botão não funcionar, copie e cole este link no navegador:<br>$botaoUrl</p>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</body>
		</html>
	""".trimIndent()
}
