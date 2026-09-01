package com.studioflow.backend.servico

import com.studioflow.backend.security.SecurityUtils
import com.studioflow.backend.servico.dto.ServicoRequest
import com.studioflow.backend.servico.dto.ServicoResponse
import com.studioflow.backend.servico.dto.toResponse
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/servicos")
class ServicoController(
	private val servicoService: ServicoService
) {

	@PostMapping
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun criar(@Valid @RequestBody request: ServicoRequest): ResponseEntity<ServicoResponse> {
		val servico = servicoService.criar(SecurityUtils.currentUserId(), request)
		return ResponseEntity.status(HttpStatus.CREATED).body(servico.toResponse())
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun listarMeus(): ResponseEntity<List<ServicoResponse>> =
		ResponseEntity.ok(servicoService.listarMeus(SecurityUtils.currentUserId()).map { it.toResponse() })

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun atualizar(@PathVariable id: String, @Valid @RequestBody request: ServicoRequest): ResponseEntity<ServicoResponse> =
		ResponseEntity.ok(servicoService.atualizar(SecurityUtils.currentUserId(), id, request).toResponse())

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun remover(@PathVariable id: String): ResponseEntity<Void> {
		servicoService.remover(SecurityUtils.currentUserId(), id)
		return ResponseEntity.noContent().build()
	}

	@GetMapping("/estabelecimento/{estabelecimentoId}")
	fun listarPorEstabelecimento(@PathVariable estabelecimentoId: String): ResponseEntity<List<ServicoResponse>> =
		ResponseEntity.ok(servicoService.listarPorEstabelecimento(estabelecimentoId).map { it.toResponse() })

	@GetMapping("/{id}")
	fun buscarPorId(@PathVariable id: String): ResponseEntity<ServicoResponse> =
		ResponseEntity.ok(servicoService.buscarPorId(id).toResponse())
}
