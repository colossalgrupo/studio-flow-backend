package com.studioflow.backend.pagamento

import com.studioflow.backend.pagamento.dto.ConfirmarPagamentoRequest
import com.studioflow.backend.pagamento.dto.PagamentoResponse
import com.studioflow.backend.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pagamentos")
class PagamentoController(
	private val pagamentoService: PagamentoService
) {

	@PostMapping("/confirmar")
	@PreAuthorize("hasRole('CLIENTE')")
	fun confirmar(@Valid @RequestBody request: ConfirmarPagamentoRequest): ResponseEntity<PagamentoResponse> =
		ResponseEntity.ok(pagamentoService.confirmarPagamento(SecurityUtils.currentUserId(), request))
}
