package com.nasportfolio.services.security

import at.favre.lib.crypto.bcrypt.BCrypt.Hasher
import at.favre.lib.crypto.bcrypt.BCrypt.Verifyer

class BcryptHashingService(
    private val hasher: Hasher,
    private val verifier: Verifyer
) : HashingService {
    override fun hash(value: String): String {
        return hasher.hashToString(12, value.toCharArray())
    }

    override fun verify(value: String, hashedValue: String): Boolean {
        return verifier.verify(value.toCharArray(), hashedValue).verified
    }
}