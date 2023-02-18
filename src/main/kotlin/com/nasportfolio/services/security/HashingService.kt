package com.nasportfolio.services.security

interface HashingService {
    fun hash(value: String): String
    fun verify(value: String, hashedValue: String): Boolean
}