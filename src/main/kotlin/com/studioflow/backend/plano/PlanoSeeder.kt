package com.studioflow.backend.plano

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PlanoSeeder(
	private val planoRepository: PlanoRepository
) : CommandLineRunner {

	override fun run(vararg args: String?) {
		seedPlano(nome = "Standard", precoMensal = BigDecimal("49.90"), taxaPlataformaPct = BigDecimal("5.0"), limiteProfissionais = 3)
		seedPlano(nome = "Black", precoMensal = BigDecimal("89.90"), taxaPlataformaPct = BigDecimal("2.5"), limiteProfissionais = 10)
		seedPlano(nome = "Diamond", precoMensal = BigDecimal("189.90"), taxaPlataformaPct = BigDecimal("1.5"), limiteProfissionais = null)
	}

	private fun seedPlano(nome: String, precoMensal: BigDecimal, taxaPlataformaPct: BigDecimal, limiteProfissionais: Int?) {
		if (planoRepository.findByNome(nome) == null) {
			planoRepository.save(
				Plano(
					nome = nome,
					precoMensal = precoMensal,
					taxaPlataformaPct = taxaPlataformaPct,
					limiteProfissionais = limiteProfissionais
				)
			)
		}
	}
}
