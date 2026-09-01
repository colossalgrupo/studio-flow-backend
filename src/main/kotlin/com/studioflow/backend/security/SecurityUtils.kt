package com.studioflow.backend.security

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {

	fun currentUser(): UserPrincipal =
		SecurityContextHolder.getContext().authentication.principal as UserPrincipal

	fun currentUserId(): String = currentUser().id
}
