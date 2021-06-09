package com.mytlogos.enterprise.background.repository

import android.app.Application
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.UpdateUser
import com.mytlogos.enterprise.model.User
import com.mytlogos.enterprise.tools.SingletonHolder
import kotlinx.coroutines.runBlocking
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
class UserRepository private constructor(application: Application) {
    private val userDao = AbstractDatabase.getInstance(application).userDao()

    private val editService: EditService
        get() = (RepositoryImpl.instance as RepositoryImpl).editService

    private val persister: ClientModelPersister
        get() = (RepositoryImpl.instance as RepositoryImpl).getPersister()

    suspend fun updateUser(updateUser: UpdateUser) {
        editService.updateUser(updateUser)
    }

    fun deleteAllUser() {
        TaskManager.runTaskSuspend { userDao.deleteAllUser() }
    }

    fun getUserNow(): User? {
        val converter = RoomConverter()
        return converter.convert(runBlocking { userDao.getUserNow() })
    }

    /**
     * Synchronous Login.
     *
     * @param email    email or name of the user
     * @param password password of the user
     * @throws IOException if an connection problem arose
     */
    @Throws(IOException::class)
    suspend fun login(email: String, password: String) {
        val response = client.login(email, password)
        val user = response.body()
        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            this.client.setAuthentication(user.uuid, user.session)
        }
        persister.persist(user)
    }

    /**
     * Synchronous Registration.
     *
     * @param email    email or name of the user
     * @param password password of the user
     */
    @Throws(IOException::class)
    suspend fun register(email: String, password: String) {
        val response = client.register(email, password)
        val user = response.body()
        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            this.client.setAuthentication(user.uuid, user.session)
        }
        persister.persist(user).finish()
    }

    fun logout() {
        TaskManager.runTaskSuspend {
            try {
                val response = client.logout()
                if (!response.isSuccessful) {
                    println("Log out was not successful: " + response.message())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            userDao.deleteAllUser()
        }
    }


    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    companion object : SingletonHolder<UserRepository, Application>(::UserRepository)
}