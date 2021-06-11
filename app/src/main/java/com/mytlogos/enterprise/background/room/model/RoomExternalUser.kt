package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = RoomUser::class, childColumns = arrayOf("userUuid"), parentColumns = arrayOf("uuid"), onDelete = ForeignKey.CASCADE)], indices = [Index(value = arrayOf("uuid")), Index(value = arrayOf("userUuid"))])
data class RoomExternalUser(
    @field:PrimaryKey val uuid: String,
    val userUuid: String,
    val identifier: String,
    val type: Int,
)