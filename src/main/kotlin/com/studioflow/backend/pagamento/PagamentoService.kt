package com.studioflow.backend.pagamento

import com.studioflow.backend.agendamento.AgendamentoService
import com.studioflow.backend.agendamento.StatusAgendamento
import com.studioflow.backend.common.exception.BusinessException
import com.studioflow.backend.estabelecimento.EstabelecimentoRepository
import com.studioflow.backend.pagamento.dto.ConfirmarPagamentoRequest
import com.studioflow.backend.pagamento.dto.PagamentoResponse
import com.studioflow.backend.pagamento.dto.toResponse
import com.studioflow.backend.pagamento.gateway.PaymentGateway
import com.studioflow.backend.pagamento.gateway.PaymentGatewayRequest
import com.studioflow.backend.plano.PlanoRepository
import com.studioflow.backend.profissional.ProfissionalRepository
import com.studioflow.backend.servico.ServicoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PagamentoService(
	private val pagamentoRepository: PagamentoRepository,
	private val splitPagamentoRepository: SplitPagamentoRepository,
	private val agendamentoService: AgendamentoService,
	private val servicoRepository: ServicoRepository,
	private val profissionalRepository: ProfissionalRepository,
	private val estabelecimentoRepository: EstabelecimentoRepository,
	private val planoRepository: PlanoRepository,
	private val paymentGateway: PaymentGateway
) {

	fun confirmarPagamento(clienteId: String, request: ConfirmarPagamentoRequest): PagamentoResponse {
		val agendamento = agendamentoService.buscarPorId(request.agendamentoId)
		if (agendamento.clienteId != clienteId) {
			throw BusinessException(HttpStatus.FORBIDDEN, "Agendamento não pertence ao usuário autenticado")
		}
		if (pagamentoRepository.findByAgendamentoId(agendamento.id!!) != null) {
			throw BusinessException(HttpStatus.CONFLICT, "Este agendamento já possui um pagamento registrado")
		}

		val servico = servicoRepository.findById(agendamento.servicoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Serviço não encontrado") }
		val profissional = profissionalRepository.findById(agendamento.profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		val estabelecimento = estabelecimentoRepository.findById(profissional.estabelecimentoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado") }
		val plano = planoRepository.findById(estabelecimento.planoId)
			.orElseThrow { BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Plano do estabelecimento não encontrado") }

		val valorTotal = servico.precoBase

		val resultado = paymentGateway.processarPagamento(
			PaymentGatewayRequest(
				valor = valorTotal,
				metodo = request.metodo,
				referenciaExterna = agendamento.id
			)
		)

		val pagamento = pagamentoRepository.save(
			Pagamento(
				agendamentoId = agendamento.id,
				valorTotal = valorTotal,
				metodo = request.metodo,
				statusPsp = resultado.statusPsp,
				status = if (resultado.aprovado) StatusPagamento.APROVADO else StatusPagamento.RECUSADO
			)
		)

		if (!resultado.aprovado) {
			return pagamento.toResponse(null)
		}

		val split = calcularSplit(
			pagamentoId = pagamento.id!!,
			valorTotal = valorTotal,
			taxaPlataformaPct = plano.taxaPlataformaPct,
			percentualComissaoProfissional = profissional.percentualComissao
		)
		val splitSalvo = splitPagamentoRepository.save(split)

		agendamentoService.atualizarStatus(agendamento.id, StatusAgendamento.CONFIRMADO)

		return pagamento.toResponse(splitSalvo.toResponse())
	}

	private fun calcularSplit(
		pagamentoId: String,
		valorTotal: BigDecimal,
		taxaPlataformaPct: BigDecimal,
		percentualComissaoProfissional: BigDecimal
	): SplitPagamento {
		val valorPlataforma = arredondar(valorTotal * taxaPlataformaPct / BigDecimal(100))
		val valorProfissional = arredondar(valorTotal * percentualComissaoProfissional / BigDecimal(100))
		val valorEstabelecimento = arredondar(valorTotal - valorPlataforma - valorProfissional)

		return SplitPagamento(
			pagamentoId = pagamentoId,
			valorPlataforma = valorPlataforma,
			valorEstabelecimento = valorEstabelecimento,
			valorProfissional = valorProfissional
		)
	}

	private fun arredondar(valor: BigDecimal): BigDecimal = valor.setScale(2, RoundingMode.HALF_UP)
}
