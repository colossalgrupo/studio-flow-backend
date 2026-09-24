package com.studioflow.backend.whatsapp.agente

import com.studioflow.backend.plano.PlanoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AgenteVendasService(
	private val conversaRepository: ConversaRepository,
	private val anthropicClient: AnthropicClient,
	private val whatsAppMessageSender: WhatsAppMessageSender,
	private val planoRepository: PlanoRepository
) {
	private val log = LoggerFactory.getLogger(AgenteVendasService::class.java)

	private val mensagemFallback =
		"Desculpa, tive um problema técnico aqui agora. Pode repetir sua mensagem em instantes? " +
			"Enquanto isso, você pode conhecer o Studio Schedulle em https://www.studioschedulle.com.br"

	fun processarMensagemRecebida(waId: String, nomeContato: String?, textoRecebido: String) {
		val conversaAtual = conversaRepository.findByWaId(waId)
			?: Conversa(waId = waId, nomeContato = nomeContato)

		val historico = conversaAtual.mensagens + MensagemConversa(papel = "user", texto = textoRecebido)

		val resposta = anthropicClient.gerarResposta(montarSystemPrompt(), historico) ?: mensagemFallback

		val historicoAtualizado = (historico + MensagemConversa(papel = "assistant", texto = resposta))
			.takeLast(40) // limita o contexto guardado por conversa

		conversaRepository.save(
			conversaAtual.copy(
				nomeContato = nomeContato ?: conversaAtual.nomeContato,
				mensagens = historicoAtualizado,
				atualizadoEm = Instant.now()
			)
		)

		log.info("Agente respondeu para {}: {}", waId, resposta.take(120))
		whatsAppMessageSender.enviarTexto(waId, resposta)
	}

	private fun montarSystemPrompt(): String {
		val planos = planoRepository.findAll().sortedBy { it.precoMensal }
		val planosDescricao = planos.joinToString("\n") { plano ->
			val limite = plano.limiteProfissionais?.let { "até $it profissionais" } ?: "profissionais ilimitados"
			"- ${plano.nome}: R$ ${plano.precoMensal}/mês, taxa de ${plano.taxaPlataformaPct}% por transação, $limite"
		}.ifBlank { "- Standard: R$ 49,90/mês, taxa de 5%, até 3 profissionais\n- Black: R$ 89,90/mês, taxa de 2,5%, até 10 profissionais\n- Diamond: R$ 189,90/mês, taxa de 1,5%, profissionais ilimitados" }

		return """
			Você é o agente de vendas do Studio Schedulle no WhatsApp, respondendo em português do Brasil.

			SOBRE O PRODUTO:
			Studio Schedulle é uma plataforma de agendamento com pagamento integrado (Pix e cartão, com
			split automático) para barbearias, estúdios de pilates, personal trainers, salões de manicure,
			podólogas, massoterapeutas, trancistas e outros profissionais de beleza e bem-estar autônomos.
			O cliente final marca e paga direto pelo app; o valor é dividido automaticamente entre o
			estabelecimento e o profissional que atendeu, conforme o percentual de comissão configurado.
			Cada profissional cadastra sua própria chave Pix ou dados bancários, então o repasse não
			precisa ser feito manualmente.

			PLANOS DISPONÍVEIS:
			$planosDescricao

			SEU OBJETIVO NESSA CONVERSA:
			1. Se apresentar brevemente e entender o negócio da pessoa (que tipo de serviço presta,
			   quantos profissionais trabalham com ela hoje).
			2. Recomendar o plano mais adequado ao tamanho do negócio.
			3. Tirar dúvidas sobre split de pagamento, Pix, taxas, repasse aos profissionais — com base
			   apenas nas informações acima, sem inventar recursos que não foram descritos.
			4. Quando a pessoa demonstrar interesse ou pedir para começar, envie o link de cadastro:
			   https://www.studioschedulle.com.br/?plano=<id-do-plano>#assinar
			   (troque <id-do-plano> por "standard", "black" ou "diamond" conforme o plano recomendado;
			   se não tiver certeza do plano, use o link sem o parâmetro: https://www.studioschedulle.com.br/#assinar)

			COMO SE COMPORTAR:
			- Respostas curtas e diretas, como uma conversa de WhatsApp — não escreva parágrafos longos.
			- Nunca invente informações sobre o produto, preços ou prazos que não estão listados acima.
			- Se perguntarem algo que você não sabe, diga que vai verificar e sugira falar com o time
			  pelo site, sem inventar uma resposta.
			- Não peça dados sensíveis (senha, cartão de crédito) pelo WhatsApp.
		""".trimIndent()
	}
}
