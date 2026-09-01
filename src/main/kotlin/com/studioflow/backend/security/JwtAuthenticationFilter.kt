package com.studioflow.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
	private val jwtService: JwtService,
	private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		val header = request.getHeader("Authorization")
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response)
			return
		}

		val token = header.substringAfter("Bearer ").trim()
		if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
			val email = jwtService.extractSubject(token)
			val userDetails = userDetailsService.loadUserByUsername(email)
			val authentication = UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.authorities
			)
			authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
			SecurityContextHolder.getContext().authentication = authentication
		}

		filterChain.doFilter(request, response)
	}
}
