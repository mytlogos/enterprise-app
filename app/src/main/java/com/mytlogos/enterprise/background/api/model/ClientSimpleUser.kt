package com.mytlogos.enterprise.background.api.model

data class ClientSimpleUser(
    val uuid: String,
    val session: String,
    val name: String,
)