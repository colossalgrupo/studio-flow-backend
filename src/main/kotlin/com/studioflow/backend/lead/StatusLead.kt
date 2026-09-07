package com.studioflow.backend.lead

/** Estágio de um lead comercial no funil de aquisição (não confundir com o cliente final da plataforma). */
enum class StatusLead {
	NOVO,
	CONTATADO,
	RESPONDEU,
	CONVERTIDO,
	DESQUALIFICADO
}
