package com.studioflow.backend.pagamento.gateway

import com.studioflow.backend.pagamento.MetodoPagamento
import java.math.BigDecimal

data class PaymentGatewayRequest(
	val valor: BigDecimal,
	val metodo: MetodoPagamento,
	val referenciaExterna: String
)

data class PaymentGatewayResult(
	val aprovado: Boolean,
	val statusPsp: String,
	val transacaoId: String
)

/**
 * Abstração sobre o provedor de pagamento (Pix/cartão/split real).
 * A implementação real (Asaas/Pagar.me/Mercado Pago) substitui [MockPaymentGateway]
 * sem exigir mudanças no domínio.
 */
interface PaymentGateway {
	fun processarPagamento(request: PaymentGatewayRequest): PaymentGatewayResult
}
