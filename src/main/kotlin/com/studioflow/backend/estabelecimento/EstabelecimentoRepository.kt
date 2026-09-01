package com.studioflow.backend.estabelecimento

import org.springframework.data.mongodb.repository.MongoRepository

interface EstabelecimentoRepository : MongoRepository<Estabelecimento, String> {
	fun findByUsuarioDonoId(usuarioDonoId: String): Estabelecimento?
	fun existsByUsuarioDonoId(usuarioDonoId: String): Boolean
	fun findByCategoria(categoria: CategoriaEstabelecimento): List<Estabelecimento>
}
