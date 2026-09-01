package com.studioflow.backend.pagamento.gateway

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MockPaymentGateway : PaymentGateway {

	override fun processarPagamento(request: PaymentGatewayRequest): PaymentGatewayResult =
		PaymentGatewayResult(
			aprovado = true,
			statusPsp = "APROVADO_MOCK",
			transacaoId = UUID.randomUUID().toString()
		)
}
