package com.studioflow.backend.agendamento

import com.studioflow.backend.agendamento.dto.AgendamentoDetalhadoResponse
import com.studioflow.backend.agendamento.dto.AgendamentoRequest
import com.studioflow.backend.agendamento.dto.AgendamentoResponse
import com.studioflow.backend.agendamento.dto.AtualizarStatusAgendamentoRequest
import com.studioflow.backend.agendamento.dto.toResponse
import com.studioflow.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agendamentos")
class AgendamentoController(
	private val agendamentoService: AgendamentoService
) {

	@PostMapping
	@PreAuthorize("hasRole('CLIENTE')")
	fun criar(@Valid @RequestBody request: AgendamentoRequest): ResponseEntity<AgendamentoResponse> {
		val agendamento = agendamentoService.criar(SecurityUtils.currentUserId(), request)
		return ResponseEntity.status(HttpStatus.CREATED).body(agendamento.toResponse())
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('CLIENTE')")
	fun listarMeus(): ResponseEntity<List<AgendamentoResponse>> =
		ResponseEntity.ok(agendamentoService.listarMeus(SecurityUtils.currentUserId()).map { it.toResponse() })

	@GetMapping("/profissional/{profissionalId}")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun listarPorProfissional(@PathVariable profissionalId: String): ResponseEntity<List<AgendamentoResponse>> =
		ResponseEntity.ok(
			agendamentoService.listarPorProfissionalDoDono(SecurityUtils.currentUserId(), profissionalId)
				.map { it.toResponse() }
		)

	@GetMapping("/estabelecimento")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun listarPorEstabelecimento(): ResponseEntity<List<AgendamentoDetalhadoResponse>> =
		ResponseEntity.ok(agendamentoService.listarPorEstabelecimentoDoDono(SecurityUtils.currentUserId()))

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('EMPREENDEDOR')")
	fun atualizarStatus(
		@PathVariable id: String,
		@Valid @RequestBody request: AtualizarStatusAgendamentoRequest
	): ResponseEntity<AgendamentoResponse> =
		ResponseEntity.ok(
			agendamentoService.atualizarStatusDoEstabelecimento(SecurityUtils.currentUserId(), id, request.status).toResponse()
		)
}
