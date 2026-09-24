package com.studioflow.backend.assinatura

import com.studioflow.backend.asaas.AsaasClienteService
import com.studioflow.backend.asaas.AsaasClient
import com.studioflow.backend.asaas.dto.AsaasAssinaturaRequest
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.plano.PlanoRepository
import com.studioflow.backend.usuario.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class AssinaturaService(
	private val assinaturaRepository: AssinaturaRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val usuarioRepository: UsuarioRepository,
	private val planoRepository: PlanoRepository,
	private val asaasClienteService: AsaasClienteService,
	private val asaasClient: AsaasClient,
	@Value("\${studioflow.pagamento.gateway-ativo:false}") private val gatewayAtivo: Boolean
) {
	private val log = LoggerFactory.getLogger(AssinaturaService::class.java)

	fun ativarPlano(estabelecimentoId: String, planoId: String): Assinatura {
		val existente = assinaturaRepository.findByEstabelecimentoId(estabelecimentoId)
		val assinatura = (existente ?: Assinatura(
			estabelecimentoId = estabelecimentoId,
			planoId = planoId,
			status = StatusAssinatura.ATIVA,
			cicloAtual = cicloAtual()
		)).copy(planoId = planoId, status = StatusAssinatura.ATIVA, cicloAtual = cicloAtual())

		val jaTinhaAssinaturaAsaas = existente?.asaasSubscriptionId != null
		val salva = assinaturaRepository.save(assinatura)

		if (gatewayAtivo && !jaTinhaAssinaturaAsaas) {
			val subscriptionId = criarAssinaturaAsaas(estabelecimentoId, planoId)
			if (subscriptionId != null) {
				return assinaturaRepository.save(salva.copy(asaasSubscriptionId = subscriptionId))
			}
		} else if (gatewayAtivo && jaTinhaAssinaturaAsaas) {
			log.warn(
				"Troca de plano do estabelecimento {} não atualiza o valor da assinatura Asaas automaticamente ainda (assinatura {})",
				estabelecimentoId,
				existente?.asaasSubscriptionId
			)
		}

		return salva
	}

	fun buscarPorEstabelecimento(estabelecimentoId: String): Assinatura? =
		assinaturaRepository.findByEstabelecimentoId(estabelecimentoId)

	/** Cria a cobrança recorrente (mensalidade da plataforma) na Asaas. Retorna null e só loga se falhar —
	 * nunca bloqueia a ativação local do plano por causa disso. */
	private fun criarAssinaturaAsaas(estabelecimentoId: String, planoId: String): String? {
		val estabelecimento = estabelecimentoRepository.findById(estabelecimentoId).orElse(null) ?: return null
		val dono = usuarioRepository.findById(estabelecimento.usuarioDonoId).orElse(null) ?: return null
		val plano = planoRepository.findById(planoId).orElse(null) ?: return null

		if (estabelecimento.cpfCnpj.isBlank()) {
			log.warn("Estabelecimento {} sem CPF/CNPJ — assinatura Asaas não criada", estabelecimentoId)
			return null
		}

		val customerId = asaasClienteService.garantirCliente(dono, estabelecimento.cpfCnpj) ?: run {
			log.warn("Não foi possível criar cliente Asaas do dono do estabelecimento {}", estabelecimentoId)
			return null
		}

		val resposta = asaasClient.criarAssinatura(
			AsaasAssinaturaRequest(
				customer = customerId,
				billingType = "PIX",
				value = plano.precoMensal,
				cycle = "MONTHLY",
				nextDueDate = LocalDate.now(),
				externalReference = estabelecimentoId
			)
		)
		if (resposta?.id == null) {
			log.warn("Falha ao criar assinatura Asaas do estabelecimento {}", estabelecimentoId)
			return null
		}
		log.info("Assinatura Asaas {} criada pro estabelecimento {}", resposta.id, estabelecimentoId)
		return resposta.id
	}

	private fun cicloAtual(): String = YearMonth.now().toString()
}
