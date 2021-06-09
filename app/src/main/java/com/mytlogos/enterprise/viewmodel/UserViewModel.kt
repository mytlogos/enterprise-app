package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.repository.UserRepository
import com.mytlogos.enterprise.model.HomeStats
import com.mytlogos.enterprise.model.UpdateUser
import com.mytlogos.enterprise.model.User
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
class UserViewModel(application: Application) : RepoViewModel(application) {
    private val userRepository by lazy { UserRepository.getInstance(application) }
    val userLiveData: LiveData<User?> by lazy { repository.user }
    val homeStatsLiveData: LiveData<HomeStats> by lazy { repository.homeStats }

    val isLoading: Boolean
        get() = repository.isLoading

    suspend fun updateUser(updateUser: UpdateUser) {
        userRepository.updateUser(updateUser)
    }

    @Throws(IOException::class)
    suspend fun login(user: String, password: String) {
        userRepository.login(user, password)
    }

    @Throws(IOException::class)
    suspend fun register(user: String, password: String) {
        userRepository.register(user, password)
    }

    fun logout() {
        userRepository.logout()
    }

    override fun onCleared() {
        // todo clean up
    }
}