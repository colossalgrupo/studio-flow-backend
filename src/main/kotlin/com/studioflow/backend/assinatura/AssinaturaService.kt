package com.studioflow.backend.assinatura

import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class AssinaturaService(
	private val assinaturaRepository: AssinaturaRepository
) {

	fun ativarPlano(estabelecimentoId: String, planoId: String): Assinatura {
		val existente = assinaturaRepository.findByEstabelecimentoId(estabelecimentoId)
		val assinatura = (existente ?: Assinatura(
			estabelecimentoId = estabelecimentoId,
			planoId = planoId,
			status = StatusAssinatura.ATIVA,
			cicloAtual = cicloAtual()
		)).copy(planoId = planoId, status = StatusAssinatura.ATIVA, cicloAtual = cicloAtual())

		return assinaturaRepository.save(assinatura)
	}

	fun buscarPorEstabelecimento(estabelecimentoId: String): Assinatura? =
		assinaturaRepository.findByEstabelecimentoId(estabelecimentoId)

	private fun cicloAtual(): String = YearMonth.now().toString()
}
