package com.mytlogos.enterprise.background.room.model

import androidx.room.Embedded

class RoomListView(@field:Embedded val mediaList: RoomMediaList, val size: Int)