package com.studioflow.backend.financeiro

import com.studioflow.backend.agendamento.Agendamento
import com.studioflow.backend.agendamento.AgendamentoRepository
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.Estabelecimento
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.financeiro.dto.RepasseResponse
import com.studioflow.backend.financeiro.dto.TransacaoResponse
import com.studioflow.backend.financeiro.dto.toResponse
import com.studioflow.backend.pagamento.Pagamento
import com.studioflow.backend.pagamento.PagamentoRepository
import com.studioflow.backend.pagamento.SplitPagamento
import com.studioflow.backend.pagamento.SplitPagamentoRepository
import com.studioflow.backend.profissional.Profissional
import com.studioflow.backend.profissional.ProfissionalRepository
import com.studioflow.backend.servico.ServicoRepository
import com.studioflow.backend.usuario.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class FinanceiroService(
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val agendamentoRepository: AgendamentoRepository,
	private val pagamentoRepository: PagamentoRepository,
	private val splitPagamentoRepository: SplitPagamentoRepository,
	private val servicoRepository: ServicoRepository,
	private val usuarioRepository: UsuarioRepository,
	private val repasseRepository: RepasseRepository
) {

	fun listarTransacoes(usuarioDonoId: String): List<TransacaoResponse> {
		val (estabelecimento, profissionais) = estabelecimentoEProfissionais(usuarioDonoId)
		if (profissionais.isEmpty()) return emptyList()
		val profissionaisPorId = profissionais.associateBy { it.id }

		val agendamentos = agendamentoRepository.findByProfissionalIdIn(profissionais.mapNotNull { it.id })
		if (agendamentos.isEmpty()) return emptyList()
		val agendamentosPorId = agendamentos.associateBy { it.id }

		val pagamentos = pagamentoRepository.findByAgendamentoIdIn(agendamentos.mapNotNull { it.id })
		if (pagamentos.isEmpty()) return emptyList()

		val splitsPorPagamentoId = splitPagamentoRepository
			.findByPagamentoIdIn(pagamentos.mapNotNull { it.id })
			.associateBy { it.pagamentoId }

		val servicosPorId = servicoRepository.findByEstabelecimentoId(estabelecimento.id!!).associateBy { it.id }
		val clientesPorId = usuarioRepository
			.findAllById(agendamentos.map { it.clienteId }.distinct())
			.associateBy { it.id }

		return pagamentos.mapNotNull { pagamento ->
			val split = splitsPorPagamentoId[pagamento.id] ?: return@mapNotNull null
			val agendamento = agendamentosPorId[pagamento.agendamentoId] ?: return@mapNotNull null
			val servico = servicosPorId[agendamento.servicoId]
			val cliente = clientesPorId[agendamento.clienteId]
			TransacaoResponse(
				id = pagamento.id!!,
				agendamentoId = agendamento.id!!,
				data = agendamento.dataHora,
				metodo = pagamento.metodo,
				clienteNome = cliente?.nome ?: "Cliente",
				profissionalId = agendamento.profissionalId,
				profissionalNome = profissionaisPorId[agendamento.profissionalId]?.nome ?: "",
				servicoId = agendamento.servicoId,
				servicoNome = servico?.nome ?: "",
				valorTotal = pagamento.valorTotal,
				taxaPlataforma = split.valorPlataforma,
				valorEstabelecimento = split.valorEstabelecimento,
				valorProfissional = split.valorProfissional
			)
		}.sortedByDescending { it.data }
	}

	fun listarRepasses(usuarioDonoId: String): List<RepasseResponse> {
		val (_, profissionais) = estabelecimentoEProfissionais(usuarioDonoId)
		if (profissionais.isEmpty()) return emptyList()
		val profissionaisIds = profissionais.mapNotNull { it.id }
		val profissionaisPorId = profissionais.associateBy { it.id }

		val realizados = repasseRepository.findByProfissionalIdIn(profissionaisIds)
			.map { it.toResponse(profissionaisPorId[it.profissionalId]?.nome ?: "") }

		val (splitsNaoAlocados, pagamentosPorId, agendamentosPorId) = splitsPendentesDoEstabelecimento(profissionaisIds)

		val pendentesPorProfissional = mutableMapOf<String, MutableList<Pair<SplitPagamento, Agendamento>>>()
		for (split in splitsNaoAlocados) {
			val pagamento = pagamentosPorId[split.pagamentoId] ?: continue
			val agendamento = agendamentosPorId[pagamento.agendamentoId] ?: continue
			pendentesPorProfissional.getOrPut(agendamento.profissionalId) { mutableListOf() }.add(split to agendamento)
		}

		val pendentes = pendentesPorProfissional.map { (profissionalId, itens) ->
			val valorLiquido = itens.sumOf { it.first.valorProfissional }
			val valorComissao = itens.sumOf { it.first.valorPlataforma + it.first.valorEstabelecimento }
			val datas = itens.map { it.second.dataHora.toLocalDate() }
			RepasseResponse(
				id = "pendente-$profissionalId",
				profissionalId = profissionalId,
				profissionalNome = profissionaisPorId[profissionalId]?.nome ?: "",
				periodoInicio = datas.min(),
				periodoFim = datas.max(),
				valorBruto = valorLiquido + valorComissao,
				valorComissaoPlataforma = valorComissao,
				valorLiquido = valorLiquido,
				status = StatusRepasse.PENDENTE,
				dataPrevista = datas.max(),
				dataRealizado = null
			)
		}

		return (realizados + pendentes).sortedByDescending { it.periodoFim }
	}

	fun marcarRepasseComoRealizado(usuarioDonoId: String, profissionalId: String): RepasseResponse {
		val (_, profissionais) = estabelecimentoEProfissionais(usuarioDonoId)
		val profissional = profissionais.find { it.id == profissionalId }
			?: throw BusinessException(HttpStatus.FORBIDDEN, "Profissional não pertence ao seu estabelecimento")

		val (splitsNaoAlocados, pagamentosPorId, agendamentosPorId) = splitsPendentesDoEstabelecimento(listOf(profissionalId))
		if (splitsNaoAlocados.isEmpty()) {
			throw BusinessException(HttpStatus.BAD_REQUEST, "Não há repasse pendente para este profissional")
		}

		val datas = splitsNaoAlocados.mapNotNull { split ->
			pagamentosPorId[split.pagamentoId]?.let { pagamento -> agendamentosPorId[pagamento.agendamentoId]?.dataHora?.toLocalDate() }
		}
		val valorLiquido = splitsNaoAlocados.sumOf { it.valorProfissional }
		val valorComissao = splitsNaoAlocados.sumOf { it.valorPlataforma + it.valorEstabelecimento }

		val repasse = repasseRepository.save(
			Repasse(
				profissionalId = profissionalId,
				periodoInicio = datas.min(),
				periodoFim = datas.max(),
				valorBruto = valorLiquido + valorComissao,
				valorComissaoPlataforma = valorComissao,
				valorLiquido = valorLiquido,
				status = StatusRepasse.REALIZADO,
				dataPrevista = datas.max(),
				dataRealizado = LocalDate.now()
			)
		)

		splitsNaoAlocados.forEach { split -> splitPagamentoRepository.save(split.copy(repasseId = repasse.id)) }

		return repasse.toResponse(profissional.nome)
	}

	private fun splitsPendentesDoEstabelecimento(
		profissionaisIds: List<String>
	): Triple<List<SplitPagamento>, Map<String?, Pagamento>, Map<String?, Agendamento>> {
		val agendamentos = agendamentoRepository.findByProfissionalIdIn(profissionaisIds)
		val pagamentos = pagamentoRepository.findByAgendamentoIdIn(agendamentos.mapNotNull { it.id })
		val splitsNaoAlocados = splitPagamentoRepository
			.findByPagamentoIdIn(pagamentos.mapNotNull { it.id })
			.filter { it.repasseId == null }
		return Triple(splitsNaoAlocados, pagamentos.associateBy { it.id }, agendamentos.associateBy { it.id })
	}

	private fun estabelecimentoEProfissionais(usuarioDonoId: String): Pair<Estabelecimento, List<Profissional>> {
		val estabelecimento = estabelecimentoRepository.findByUsuarioDonoId(usuarioDonoId)
			?: throw BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado")
		return estabelecimento to profissionalRepository.findByEstabelecimentoId(estabelecimento.id!!)
	}
}
