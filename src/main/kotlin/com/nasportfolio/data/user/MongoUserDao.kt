package com.nasportfolio.data.user

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.regex
import java.util.regex.Pattern

class MongoUserDao(
    db: CoroutineDatabase
) : UserDao {
    private val collection = db.getCollection<User>()

    override suspend fun getAllUsers(): List<User> {
        return collection.find()
            .descendingSort(User::createdAtTimeStamp)
            .toList()
    }

    override suspend fun getUserById(id: String): User? {
        return collection.findOne(User::id eq ObjectId(id))
    }

    override suspend fun getUserByEmail(email: String): User? {
        return collection.findOne(User::email eq email)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return collection.findOne(User::username eq username)
    }

    override suspend fun searchUsersByUsername(username: String): List<User> {
        val regex = Pattern.compile("^.*$username.*$", Pattern.CASE_INSENSITIVE)
        return collection.find(User::username regex regex)
            .descendingSort(User::createdAtTimeStamp)
            .toList()
    }

    override suspend fun insertUser(user: User): Boolean {
        return collection.insertOne(user)
            .wasAcknowledged()
    }

    override suspend fun updateUser(user: User): Boolean {
        return collection.updateOne(User::id eq user.id, user)
            .wasAcknowledged()
    }

    override suspend fun deleteUser(user: User): Boolean {
        return collection.deleteOne(User::id eq user.id)
            .wasAcknowledged()
    }
}