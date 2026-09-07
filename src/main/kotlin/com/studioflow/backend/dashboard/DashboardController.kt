package com.studioflow.backend.dashboard

import com.studioflow.backend.dashboard.dto.DashboardResumoResponse
import com.studioflow.backend.security.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('EMPREENDEDOR')")
class DashboardController(
	private val dashboardService: DashboardService
) {

	@GetMapping("/resumo")
	fun resumo(): ResponseEntity<DashboardResumoResponse> =
		ResponseEntity.ok(dashboardService.obterResumo(SecurityUtils.currentUserId()))
}
