package com.mytlogos.enterprise.model

data class ExternalUser(
    val uuid: String,
    val identifier: String,
    val type: Int,
)