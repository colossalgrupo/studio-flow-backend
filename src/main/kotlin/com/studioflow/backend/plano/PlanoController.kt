package com.studioflow.backend.plano

import com.studioflow.backend.plano.dto.PlanoResponse
import com.studioflow.backend.plano.dto.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/planos")
class PlanoController(
	private val planoRepository: PlanoRepository
) {

	@GetMapping
	fun listar(): ResponseEntity<List<PlanoResponse>> =
		ResponseEntity.ok(planoRepository.findAll().map { it.toResponse() })
}
