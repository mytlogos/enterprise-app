package com.mytlogos.enterprise.background.room.model

import androidx.room.Embedded

class RoomExternListView(@field:Embedded val mediaList: RoomExternalMediaList, val size: Int)