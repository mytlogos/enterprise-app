package com.mytlogos.enterprise.background.api

import java.net.InetAddress

interface NetworkIdentificator {
    val sSID: String
    val broadcastAddress: InetAddress?
}