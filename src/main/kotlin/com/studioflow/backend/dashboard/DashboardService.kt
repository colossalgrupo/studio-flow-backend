package com.studioflow.backend.dashboard

import com.studioflow.backend.agendamento.AgendamentoRepository
import com.studioflow.backend.agendamento.StatusAgendamento
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.dashboard.dto.DashboardResumoResponse
import com.studioflow.backend.dashboard.dto.FaturamentoDiaResponse
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.financeiro.FinanceiroService
import com.studioflow.backend.financeiro.StatusRepasse
import com.studioflow.backend.pagamento.PagamentoRepository
import com.studioflow.backend.pagamento.StatusPagamento
import com.studioflow.backend.profissional.ProfissionalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DashboardService(
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val agendamentoRepository: AgendamentoRepository,
	private val pagamentoRepository: PagamentoRepository,
	private val financeiroService: FinanceiroService
) {

	fun obterResumo(usuarioDonoId: String): DashboardResumoResponse {
		val estabelecimento = estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")

		val profissionais = profissionalRepository.findByEstabelecimentoId(estabelecimento.id!!)
		val profissionaisAtivos = profissionais.count { it.ativo }

		val agendamentos = agendamentoRepository.findByProfissionalIdIn(profissionais.mapNotNull { it.id })
		val totalAgendamentos = agendamentos.count { it.status != StatusAgendamento.CANCELADO }
		val agendamentosPorId = agendamentos.associateBy { it.id }

		val pagamentosAprovados = pagamentoRepository
			.findByAgendamentoIdIn(agendamentos.mapNotNull { it.id })
			.filter { it.status == StatusPagamento.APROVADO }
		val faturamentoPeriodo = pagamentosAprovados.sumOf { it.valorTotal }

		val faturamentoPorDia = pagamentosAprovados
			.mapNotNull { pagamento -> agendamentosPorId[pagamento.agendamentoId]?.dataHora?.toLocalDate()?.let { it to pagamento.valorTotal } }
			.groupBy({ it.first }, { it.second })
			.mapValues { (_, valores) -> valores.reduce(BigDecimal::add) }
			.toSortedMap()
			.map { (data, valor) -> FaturamentoDiaResponse(data, valor) }

		val repassesPendentes = financeiroService.listarRepasses(usuarioDonoId)
			.filter { it.status == StatusRepasse.PENDENTE }
			.sumOf { it.valorLiquido }

		return DashboardResumoResponse(
			totalAgendamentosPeriodo = totalAgendamentos,
			faturamentoPeriodo = faturamentoPeriodo,
			repassesPendentes = repassesPendentes,
			profissionaisAtivos = profissionaisAtivos,
			variacaoFaturamento = BigDecimal.ZERO,
			faturamentoPorDia = faturamentoPorDia
		)
	}
}
