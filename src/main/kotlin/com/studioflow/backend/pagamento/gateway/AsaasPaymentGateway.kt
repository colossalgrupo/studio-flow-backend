package com.studioflow.backend.pagamento.gateway

import com.studioflow.backend.asaas.AsaasClient
import com.studioflow.backend.asaas.AsaasClienteService
import com.studioflow.backend.asaas.dto.AsaasCobrancaRequest
import com.studioflow.backend.asaas.dto.AsaasSplitItem
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.pagamento.MetodoPagamento
import com.studioflow.backend.usuario.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Gateway real: cria a cobrança na Asaas com o split já configurado (o estabelecimento e
 * o profissional recebem a parte deles automaticamente quando o Pix é pago — não depende
 * de nenhuma ação nossa depois). Por isso o pagamento nasce PENDENTE aqui: só vira
 * aprovado quando o webhook de confirmação chegar (ver AsaasWebhookController).
 *
 * Cartão de crédito ainda não é suportado — exigiria tokenização no app/frontend pra não
 * expor dado de cartão no nosso backend (escopo de PCI compliance), que é mudança de outro
 * repositório (studio-flow-android). Fica pra um próximo incremento.
 */
@Component
@ConditionalOnProperty(name = ["studioflow.pagamento.gateway-ativo"], havingValue = "true")
class AsaasPaymentGateway(
	private val asaasClient: AsaasClient,
	private val asaasClienteService: AsaasClienteService,
	private val usuarioRepository: UsuarioRepository
) : PaymentGateway {
	private val log = LoggerFactory.getLogger(AsaasPaymentGateway::class.java)

	override fun processarPagamento(request: PaymentGatewayRequest): PaymentGatewayResult {
		if (request.metodo != MetodoPagamento.PIX) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "Pagamento com cartão ainda não é suportado — use Pix.")
		}
		if (request.walletIdEstabelecimento.isNullOrBlank() || request.walletIdProfissional.isNullOrBlank()) {
			throw BusinessException(
				HttpStatus.CONFLICT,
				"Estabelecimento ou profissional ainda não tem subconta de pagamento configurada."
			)
		}
		if (request.clienteCpf.isNullOrBlank()) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "CPF é obrigatório pra pagar via Pix.")
		}

		val usuario = usuarioRepository.findById(request.clienteId).orElse(null)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Usuário pagador não encontrado")
		val customerId = asaasClienteService.garantirCliente(usuario, request.clienteCpf)
			?: throw BusinessException(HttpStatus.BAD_GATEWAY, "Não foi possível registrar o cliente na Asaas.")

		val cobranca = asaasClient.criarCobranca(
			AsaasCobrancaRequest(
				customer = customerId,
				billingType = "PIX",
				value = request.valor,
				dueDate = LocalDate.now(),
				externalReference = request.referenciaExterna,
				split = listOf(
					AsaasSplitItem(walletId = request.walletIdEstabelecimento, fixedValue = request.valorEstabelecimento),
					AsaasSplitItem(walletId = request.walletIdProfissional, fixedValue = request.valorProfissional)
				)
			)
		) ?: throw BusinessException(HttpStatus.BAD_GATEWAY, "Não foi possível criar a cobrança na Asaas.")

		val paymentId = cobranca.id
			?: throw BusinessException(HttpStatus.BAD_GATEWAY, "Cobrança criada na Asaas sem id retornado.")

		val qrCode = asaasClient.obterQrCodePix(paymentId)
		if (qrCode == null) {
			log.warn("Cobrança {} criada mas não foi possível obter o QR code Pix", paymentId)
		}

		return PaymentGatewayResult(
			aprovado = false,
			pendente = true,
			statusPsp = cobranca.status ?: "PENDING",
			transacaoId = paymentId,
			qrCodePayload = qrCode?.payload,
			qrCodeImagemBase64 = qrCode?.encodedImage
		)
	}
}
