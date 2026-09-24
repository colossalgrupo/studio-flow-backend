package com.studioflow.backend.asaas

import com.studioflow.backend.asaas.dto.AsaasSubcontaRequest
import com.studioflow.backend.common.crypto.CryptoService
import com.studioflow.backend.estabelecimento.Endereco
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

enum class StatusOnboardingAsaas {
	CRIADA,
	FALHA_API,
	NAO_CRIADA_FALTA_DATA_NASCIMENTO,
	NAO_CRIADA_FALTA_COMPANY_TYPE
}

data class ResultadoOnboardingAsaas(
	val accountId: String?,
	val walletId: String?,
	val apiKeyCriptografada: String?,
	val status: StatusOnboardingAsaas
)

/**
 * Cria a subconta Asaas de um estabelecimento ou profissional (necessária pra receber
 * split de pagamento — ver PaymentGateway) e captura a apiKey retornada, já criptografada,
 * porque a Asaas só devolve essa apiKey uma única vez, na criação.
 */
@Service
class AsaasOnboardingService(
	private val asaasClient: AsaasClient,
	private val cryptoService: CryptoService
) {
	private val log = LoggerFactory.getLogger(AsaasOnboardingService::class.java)

	fun criarSubconta(
		nome: String,
		email: String,
		cpfCnpj: String,
		dataNascimento: LocalDate?,
		companyType: String?,
		faturamentoMensal: BigDecimal,
		endereco: Endereco,
		telefone: String? = null
	): ResultadoOnboardingAsaas {
		val documento = cpfCnpj.filter { it.isDigit() }

		if (documento.length == 14 && companyType.isNullOrBlank()) {
			log.warn("CNPJ informado sem companyType — subconta Asaas não criada automaticamente (é preciso classificar como MEI/LIMITED/INDIVIDUAL/ASSOCIATION)")
			return ResultadoOnboardingAsaas(null, null, null, StatusOnboardingAsaas.NAO_CRIADA_FALTA_COMPANY_TYPE)
		}
		if (documento.length != 14 && dataNascimento == null) {
			log.warn("CPF informado sem data de nascimento — subconta Asaas não criada automaticamente")
			return ResultadoOnboardingAsaas(null, null, null, StatusOnboardingAsaas.NAO_CRIADA_FALTA_DATA_NASCIMENTO)
		}

		val resposta = asaasClient.criarSubconta(
			AsaasSubcontaRequest(
				name = nome,
				email = email,
				cpfCnpj = documento,
				birthDate = if (documento.length == 14) null else dataNascimento,
				companyType = if (documento.length == 14) companyType else null,
				incomeValue = faturamentoMensal,
				address = endereco.logradouro,
				addressNumber = endereco.numero,
				province = endereco.bairro,
				postalCode = endereco.cep.filter { it.isDigit() },
				mobilePhone = telefone?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }
			)
		) ?: return ResultadoOnboardingAsaas(null, null, null, StatusOnboardingAsaas.FALHA_API)

		if (resposta.walletId == null) {
			return ResultadoOnboardingAsaas(null, null, null, StatusOnboardingAsaas.FALHA_API)
		}

		val apiKeyCriptografada = resposta.apiKey?.let { apiKey ->
			if (cryptoService.configurado()) {
				cryptoService.encrypt(apiKey)
			} else {
				log.warn("CRYPTO_SECRET_KEY não configurada — apiKey da subconta Asaas NÃO foi salva (seria armazenada em texto puro)")
				null
			}
		}

		return ResultadoOnboardingAsaas(
			accountId = resposta.id,
			walletId = resposta.walletId,
			apiKeyCriptografada = apiKeyCriptografada,
			status = StatusOnboardingAsaas.CRIADA
		)
	}
}
