package com.studioflow.backend.estabelecimento

import com.studioflow.backend.estabelecimento.dto.AlterarPlanoRequest
import com.studioflow.backend.estabelecimento.dto.EstabelecimentoRequest
import com.studioflow.backend.estabelecimento.dto.EstabelecimentoResponse
import com.studioflow.backend.estabelecimento.dto.toResponse
import com.studioflow.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/estabelecimentos")
class EstabelecimentoController(
	private val estabelecimentoService: EstabelecimentoService
) {

	@PostMapping
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun criar(@Valid @RequestBody request: EstabelecimentoRequest): ResponseEntity<EstabelecimentoResponse> {
		val estabelecimento = estabelecimentoService.criar(SecurityUtils.currentUserId(), request)
		return ResponseEntity.status(HttpStatus.CREATED).body(estabelecimento.toResponse())
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun meuEstabelecimento(): ResponseEntity<EstabelecimentoResponse> =
		ResponseEntity.ok(estabelecimentoService.buscarMeuEstabelecimento(SecurityUtils.currentUserId()).toResponse())

	@PutMapping("/me")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun atualizarMeuEstabelecimento(@Valid @RequestBody request: EstabelecimentoRequest): ResponseEntity<EstabelecimentoResponse> =
		ResponseEntity.ok(estabelecimentoService.atualizar(SecurityUtils.currentUserId(), request).toResponse())

	@PutMapping("/me/plano")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun alterarPlano(@Valid @RequestBody request: AlterarPlanoRequest): ResponseEntity<EstabelecimentoResponse> =
		ResponseEntity.ok(estabelecimentoService.alterarPlano(SecurityUtils.currentUserId(), request.planoId).toResponse())

	@GetMapping
	fun buscar(@RequestParam(required = false) categoria: CategoriaEstabelecimento?): ResponseEntity<List<EstabelecimentoResponse>> =
		ResponseEntity.ok(estabelecimentoService.buscar(categoria).map { it.toResponse() })

	@GetMapping("/{id}")
	fun buscarPorId(@PathVariable id: String): ResponseEntity<EstabelecimentoResponse> =
		ResponseEntity.ok(estabelecimentoService.buscarPorId(id).toResponse())
}
