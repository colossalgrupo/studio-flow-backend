package com.studioflow.backend.bloqueio

import com.studioflow.backend.bloqueio.dto.BloqueioAgendaRequest
import com.studioflow.backend.bloqueio.dto.BloqueioAgendaResponse
import com.studioflow.backend.bloqueio.dto.toResponse
import com.studioflow.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bloqueios")
@PreAuthorize("hasRole('EMPREENDEDOR')")
class BloqueioAgendaController(
	private val bloqueioAgendaService: BloqueioAgendaService
) {

	@PostMapping
	fun criar(@Valid @RequestBody request: BloqueioAgendaRequest): ResponseEntity<BloqueioAgendaResponse> {
		val bloqueio = bloqueioAgendaService.criar(SecurityUtils.currentUserId(), request)
		return ResponseEntity.status(HttpStatus.CREATED).body(bloqueio.toResponse())
	}

	@GetMapping
	fun listar(): ResponseEntity<List<BloqueioAgendaResponse>> =
		ResponseEntity.ok(bloqueioAgendaService.listarDoEstabelecimento(SecurityUtils.currentUserId()).map { it.toResponse() })

	@DeleteMapping("/{id}")
	fun remover(@PathVariable id: String): ResponseEntity<Void> {
		bloqueioAgendaService.remover(SecurityUtils.currentUserId(), id)
		return ResponseEntity.noContent().build()
	}
}
