package com.nasportfolio.data.user

interface UserDao {
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun searchUsersByUsername(username: String): List<User>
    suspend fun insertUser(user: User): Boolean
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(user: User): Boolean
}