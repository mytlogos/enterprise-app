package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity

@Entity(primaryKeys = ["title", "medium", "link"])
class RoomMediumInWait(val title: String, val medium: Int, val link: String)