package com.autoever.grouplocationsharing.repository

import com.autoever.grouplocationsharing.model.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByPhoneNumber(phoneNumber: String): User?
}
