package com.studioflow.backend.financeiro

import com.studioflow.backend.financeiro.dto.RepasseResponse
import com.studioflow.backend.financeiro.dto.TransacaoResponse
import com.studioflow.backend.security.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/financeiro")
@PreAuthorize("hasRole('EMPREENDEDOR')")
class FinanceiroController(
	private val financeiroService: FinanceiroService
) {

	@GetMapping("/transacoes")
	fun listarTransacoes(): ResponseEntity<List<TransacaoResponse>> =
		ResponseEntity.ok(financeiroService.listarTransacoes(SecurityUtils.currentUserId()))

	@GetMapping("/repasses")
	fun listarRepasses(): ResponseEntity<List<RepasseResponse>> =
		ResponseEntity.ok(financeiroService.listarRepasses(SecurityUtils.currentUserId()))

	@PostMapping("/repasses/profissional/{profissionalId}/marcar-realizado")
	fun marcarRepasseComoRealizado(@PathVariable profissionalId: String): ResponseEntity<RepasseResponse> =
		ResponseEntity.ok(financeiroService.marcarRepasseComoRealizado(SecurityUtils.currentUserId(), profissionalId))
}
