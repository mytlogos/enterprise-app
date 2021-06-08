package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.model.HomeStats
import com.mytlogos.enterprise.model.UpdateUser
import com.mytlogos.enterprise.model.User
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
class UserViewModel(application: Application) : RepoViewModel(application) {
    val userLiveData: LiveData<User?> = repository.user
    val homeStatsLiveData: LiveData<HomeStats> = repository.homeStats

    val isLoading: Boolean
        get() = repository.isLoading

    fun updateUser(updateUser: UpdateUser) {
        repository.updateUser(updateUser)
    }

    @Throws(IOException::class)
    suspend fun login(user: String, password: String) {
        repository.login(user, password)
    }

    @Throws(IOException::class)
    suspend fun register(user: String, password: String) {
        repository.register(user, password)
    }

    fun logout() {
        repository.logout()
    }

    override fun onCleared() {
        // todo clean up
    }
}