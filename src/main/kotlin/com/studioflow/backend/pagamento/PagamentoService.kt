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
import com.studioflow.backend.usuario.UsuarioRepository
import org.slf4j.LoggerFactory
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
	private val usuarioRepository: UsuarioRepository,
	private val paymentGateway: PaymentGateway
) {
	private val log = LoggerFactory.getLogger(PagamentoService::class.java)

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
		val cliente = usuarioRepository.findById(clienteId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Cliente não encontrado") }

		val valorTotal = servico.precoBase
		// Calculado ANTES de chamar o gateway — no gateway real, o split precisa ir junto
		// no corpo da cobrança (é a Asaas que divide o dinheiro no momento do pagamento,
		// não algo que a gente faz depois por conta própria).
		val valorPlataforma = arredondar(valorTotal * plano.taxaPlataformaPct / BigDecimal(100))
		val valorProfissional = arredondar(valorTotal * profissional.percentualComissao / BigDecimal(100))
		val valorEstabelecimento = arredondar(valorTotal - valorPlataforma - valorProfissional)

		val resultado = paymentGateway.processarPagamento(
			PaymentGatewayRequest(
				valor = valorTotal,
				metodo = request.metodo,
				referenciaExterna = agendamento.id,
				clienteId = clienteId,
				clienteNome = cliente.nome,
				clienteEmail = cliente.email,
				clienteCpf = request.cpf,
				walletIdEstabelecimento = estabelecimento.asaasWalletId,
				walletIdProfissional = profissional.contaBancaria.recebedorPspId,
				valorEstabelecimento = valorEstabelecimento,
				valorProfissional = valorProfissional
			)
		)

		val status = when {
			resultado.aprovado -> StatusPagamento.APROVADO
			resultado.pendente -> StatusPagamento.PENDENTE
			else -> StatusPagamento.RECUSADO
		}

		val pagamento = pagamentoRepository.save(
			Pagamento(
				agendamentoId = agendamento.id,
				valorTotal = valorTotal,
				metodo = request.metodo,
				statusPsp = resultado.statusPsp,
				status = status,
				asaasPaymentId = if (resultado.pendente) resultado.transacaoId else null
			)
		)

		if (resultado.pendente) {
			// Ainda não houve pagamento de fato — o split só é persistido quando o
			// webhook de confirmação chegar (ver confirmarPagamentoPix).
			return pagamento.toResponse(null, resultado.qrCodePayload, resultado.qrCodeImagemBase64)
		}
		if (!resultado.aprovado) {
			return pagamento.toResponse(null)
		}

		val splitSalvo = splitPagamentoRepository.save(
			SplitPagamento(
				pagamentoId = pagamento.id!!,
				valorPlataforma = valorPlataforma,
				valorEstabelecimento = valorEstabelecimento,
				valorProfissional = valorProfissional
			)
		)

		agendamentoService.atualizarStatus(agendamento.id, StatusAgendamento.CONFIRMADO)

		return pagamento.toResponse(splitSalvo.toResponse())
	}

	/**
	 * Chamado pelo webhook da Asaas quando um Pix pendente é efetivamente pago
	 * (PAYMENT_CONFIRMED/PAYMENT_RECEIVED). Idempotente — a Asaas pode reenviar o
	 * mesmo evento (entrega "pelo menos uma vez").
	 */
	fun confirmarPagamentoPix(asaasPaymentId: String) {
		val pagamento = pagamentoRepository.findByAsaasPaymentId(asaasPaymentId) ?: run {
			log.warn("Webhook de pagamento Asaas recebido pra {} sem Pagamento correspondente", asaasPaymentId)
			return
		}
		if (pagamento.status != StatusPagamento.PENDENTE) {
			log.info("Pagamento {} já estava em status {} — ignorando webhook duplicado", pagamento.id, pagamento.status)
			return
		}

		val agendamento = agendamentoService.buscarPorId(pagamento.agendamentoId)
		val profissional = profissionalRepository.findById(agendamento.profissionalId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Profissional não encontrado") }
		val estabelecimento = estabelecimentoRepository.findById(profissional.estabelecimentoId)
			.orElseThrow { BusinessException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado") }
		val plano = planoRepository.findById(estabelecimento.planoId)
			.orElseThrow { BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Plano do estabelecimento não encontrado") }

		val split = calcularSplit(
			pagamentoId = pagamento.id!!,
			valorTotal = pagamento.valorTotal,
			taxaPlataformaPct = plano.taxaPlataformaPct,
			percentualComissaoProfissional = profissional.percentualComissao
		)
		splitPagamentoRepository.save(split)

		pagamentoRepository.save(pagamento.copy(status = StatusPagamento.APROVADO, statusPsp = "CONFIRMED"))
		agendamentoService.atualizarStatus(agendamento.id!!, StatusAgendamento.CONFIRMADO)
		log.info("Pagamento {} confirmado via webhook Asaas (Pix pago)", pagamento.id)
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
