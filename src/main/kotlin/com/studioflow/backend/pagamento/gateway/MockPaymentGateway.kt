package com.studioflow.backend.pagamento.gateway

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(name = ["studioflow.pagamento.gateway-ativo"], havingValue = "false", matchIfMissing = true)
class MockPaymentGateway : PaymentGateway {

	override fun processarPagamento(request: PaymentGatewayRequest): PaymentGatewayResult =
		PaymentGatewayResult(
			aprovado = true,
			statusPsp = "APROVADO_MOCK",
			transacaoId = UUID.randomUUID().toString()
		)
}
