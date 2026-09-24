package com.studioflow.backend.pagamento.gateway

import com.studioflow.backend.pagamento.MetodoPagamento
import java.math.BigDecimal

data class PaymentGatewayRequest(
	val valor: BigDecimal,
	val metodo: MetodoPagamento,
	val referenciaExterna: String,
	// Campos usados só pelo gateway real (Asaas) — o mock ignora tudo isso.
	val clienteId: String = "",
	val clienteNome: String = "",
	val clienteEmail: String = "",
	val clienteCpf: String? = null,
	val walletIdEstabelecimento: String? = null,
	val walletIdProfissional: String? = null,
	val valorEstabelecimento: BigDecimal = BigDecimal.ZERO,
	val valorProfissional: BigDecimal = BigDecimal.ZERO
)

data class PaymentGatewayResult(
	val aprovado: Boolean,
	val statusPsp: String,
	val transacaoId: String,
	// true quando a cobrança foi criada mas ainda depende de ação do cliente (ex.: Pix
	// aguardando pagamento) — nesse caso aprovado é false, mas não é uma recusa.
	val pendente: Boolean = false,
	val qrCodePayload: String? = null,
	val qrCodeImagemBase64: String? = null
)

/**
 * Abstração sobre o provedor de pagamento (Pix/cartão/split real).
 * A implementação real (Asaas/Pagar.me/Mercado Pago) substitui [MockPaymentGateway]
 * sem exigir mudanças no domínio.
 */
interface PaymentGateway {
	fun processarPagamento(request: PaymentGatewayRequest): PaymentGatewayResult
}
