package com.studioflow.backend.whatsapp

import org.springframework.data.mongodb.repository.MongoRepository

interface WhatsAppEventoRepository : MongoRepository<WhatsAppEvento, String>
