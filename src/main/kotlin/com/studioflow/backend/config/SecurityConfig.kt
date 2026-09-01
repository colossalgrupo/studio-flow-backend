package com.studioflow.backend.config

import com.studioflow.backend.security.CustomUserDetailsService
import com.studioflow.backend.security.JwtAuthenticationFilter
import com.studioflow.backend.security.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
	private val jwtService: JwtService,
	private val userDetailsService: CustomUserDetailsService
) {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf { it.disable() }
			.cors { it.configurationSource(corsConfigurationSource()) }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { auth ->
				auth
					.requestMatchers("/api/auth/**").permitAll()
					.requestMatchers("/actuator/health").permitAll()
					.requestMatchers(
						org.springframework.http.HttpMethod.GET,
						"/api/estabelecimentos",
						"/api/estabelecimentos/{id}",
						"/api/profissionais/buscar",
						"/api/profissionais/{id}",
						"/api/servicos/estabelecimento/{estabelecimentoId}",
						"/api/servicos/{id}",
						"/api/horarios/profissional/{profissionalId}"
					).permitAll()
					.anyRequest().authenticated()
			}
			.addFilterBefore(
				JwtAuthenticationFilter(jwtService, userDetailsService),
				UsernamePasswordAuthenticationFilter::class.java
			)

		return http.build()
	}

	private fun corsConfigurationSource(): CorsConfigurationSource {
		val configuration = CorsConfiguration().apply {
			allowedOriginPatterns = listOf("*")
			allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			allowedHeaders = listOf("*")
		}
		val source = UrlBasedCorsConfigurationSource()
		source.registerCorsConfiguration("/**", configuration)
		return source
	}
}
