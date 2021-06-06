package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomExternalUser
import com.mytlogos.enterprise.model.ExternalUser

@Dao
interface ExternalUserDao : MultiBaseDao<RoomExternalUser> {
    @Query("SELECT uuid FROM RoomExternalUser;")
    suspend fun loaded(): List<String>

    @get:Query("SELECT RoomExternalUser.uuid,RoomExternalUser.identifier, RoomExternalUser.type FROM RoomExternalUser")
    val all: DataSource.Factory<Int, ExternalUser>

    @Query("SELECT COUNT(uuid) FROM RoomExternalUser")
    fun countUser(): LiveData<Int>

    @Query("DELETE FROM RoomExternalUser WHERE uuid IN (:uuids)")
    suspend fun delete(uuids: Collection<String>)
}