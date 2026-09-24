package com.studioflow.backend.asaas

import com.studioflow.backend.asaas.dto.AsaasClienteRequest
import com.studioflow.backend.usuario.Usuario
import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.stereotype.Service

/**
 * Garante que um usuário tenha um cliente Asaas (necessário pra criar qualquer cobrança
 * ou assinatura em nome dele — seja o cliente final pagando um agendamento, seja o dono
 * do estabelecimento pagando a mensalidade da plataforma). Criado sob demanda e cacheado
 * em Usuario.asaasCustomerId.
 */
@Service
class AsaasClienteService(
	private val asaasClient: AsaasClient,
	private val usuarioRepository: UsuarioRepository
) {
	fun garantirCliente(usuario: Usuario, cpfCnpj: String): String? {
		usuario.asaasCustomerId?.let { return it }

		val resposta = asaasClient.criarCliente(
			AsaasClienteRequest(
				name = usuario.nome,
				email = usuario.email,
				cpfCnpj = cpfCnpj.filter { it.isDigit() }
			)
		) ?: return null

		val customerId = resposta.id ?: return null
		usuarioRepository.save(usuario.copy(asaasCustomerId = customerId))
		return customerId
	}
}
