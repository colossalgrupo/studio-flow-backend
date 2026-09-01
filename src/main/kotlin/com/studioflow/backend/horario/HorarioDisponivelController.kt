package com.studioflow.backend.horario

import com.studioflow.backend.horario.dto.HorarioDisponivelRequest
import com.studioflow.backend.horario.dto.HorarioDisponivelResponse
import com.studioflow.backend.horario.dto.toResponse
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/horarios")
class HorarioDisponivelController(
	private val horarioDisponivelService: HorarioDisponivelService
) {

	@PostMapping("/profissional/{profissionalId}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun criar(
		@PathVariable profissionalId: String,
		@Valid @RequestBody request: HorarioDisponivelRequest
	): ResponseEntity<HorarioDisponivelResponse> {
		val horario = horarioDisponivelService.criar(SecurityUtils.currentUserId(), profissionalId, request)
		return ResponseEntity.status(HttpStatus.CREATED).body(horario.toResponse())
	}

	@GetMapping("/profissional/{profissionalId}")
	fun listarPorProfissional(@PathVariable profissionalId: String): ResponseEntity<List<HorarioDisponivelResponse>> =
		ResponseEntity.ok(horarioDisponivelService.listarPorProfissional(profissionalId).map { it.toResponse() })

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun atualizar(
		@PathVariable id: String,
		@Valid @RequestBody request: HorarioDisponivelRequest
	): ResponseEntity<HorarioDisponivelResponse> =
		ResponseEntity.ok(horarioDisponivelService.atualizar(SecurityUtils.currentUserId(), id, request).toResponse())

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun remover(@PathVariable id: String): ResponseEntity<Void> {
		horarioDisponivelService.remover(SecurityUtils.currentUserId(), id)
		return ResponseEntity.noContent().build()
	}
}
