package com.studioflow.backend.pagamento

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document(collection = "pagamentos")
data class Pagamento(
	@Id val id: String? = null,
	@Indexed(unique = true) val agendamentoId: String,
	val valorTotal: BigDecimal,
	val metodo: MetodoPagamento,
	val statusPsp: String,
	val status: StatusPagamento
)
