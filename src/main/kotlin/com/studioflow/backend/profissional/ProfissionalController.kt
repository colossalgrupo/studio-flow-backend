package com.studioflow.backend.profissional

import com.studioflow.backend.profissional.dto.ProfissionalRequest
import com.studioflow.backend.profissional.dto.ProfissionalResponse
import com.studioflow.backend.profissional.dto.toResponse
import com.studioflow.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profissionais")
class ProfissionalController(
	private val profissionalService: ProfissionalService
) {

	@PostMapping
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun criar(@Valid @RequestBody request: ProfissionalRequest): ResponseEntity<ProfissionalResponse> {
		val profissional = profissionalService.criar(SecurityUtils.currentUserId(), request)
		return ResponseEntity.status(HttpStatus.CREATED).body(profissional.toResponse())
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun listarMeus(): ResponseEntity<List<ProfissionalResponse>> =
		ResponseEntity.ok(profissionalService.listarMeus(SecurityUtils.currentUserId()).map { it.toResponse() })

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun atualizar(@PathVariable id: String, @Valid @RequestBody request: ProfissionalRequest): ResponseEntity<ProfissionalResponse> =
		ResponseEntity.ok(profissionalService.atualizar(SecurityUtils.currentUserId(), id, request).toResponse())

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun remover(@PathVariable id: String): ResponseEntity<Void> {
		profissionalService.remover(SecurityUtils.currentUserId(), id)
		return ResponseEntity.noContent().build()
	}

	@GetMapping("/buscar")
	fun buscarPublico(
		@RequestParam(required = false) estabelecimentoId: String?,
		@RequestParam(required = false) especialidade: String?
	): ResponseEntity<List<ProfissionalResponse>> =
		ResponseEntity.ok(profissionalService.buscarPublico(estabelecimentoId, especialidade).map { it.toResponse() })

	@GetMapping("/{id}")
	fun buscarPorId(@PathVariable id: String): ResponseEntity<ProfissionalResponse> =
		ResponseEntity.ok(profissionalService.buscarPorId(id).toResponse())
}
