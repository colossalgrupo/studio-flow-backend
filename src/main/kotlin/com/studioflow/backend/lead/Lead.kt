package com.studioflow.backend.lead

import com.studioflow.backend.estabelecimento.CategoriaEstabelecimento
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Lead comercial descoberto via Google Places API para prospecção do Studio Schedulle
 * (negócio de terceiro, não é um Estabelecimento cadastrado na plataforma).
 */
@Document(collection = "leads")
data class Lead(
	@Id val id: String? = null,
	@Indexed(unique = true) val placeId: String,
	val nome: String,
	val categoria: CategoriaEstabelecimento,
	val telefone: String? = null,
	val site: String? = null,
	val endereco: String,
	@Indexed val cidade: String,
	val uf: String,
	val avaliacao: Double? = null,
	val totalAvaliacoes: Int? = null,
	@Indexed val status: StatusLead = StatusLead.NOVO,
	val criadoEm: Instant = Instant.now()
)
