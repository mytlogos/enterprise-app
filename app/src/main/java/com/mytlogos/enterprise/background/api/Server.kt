package com.mytlogos.enterprise.background.api

import android.annotation.SuppressLint
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal class Server(val ipv4: String, val port: Int, val isLocal: Boolean, val isDevServer: Boolean) {

    //allow unsafe connections for now in local networks
    @get:SuppressLint("DefaultLocale")
    val address: String
        get() {
            //allow unsafe connections for now in local networks
            val format = "http" + (if (isLocal) "" else "s") + "://%s:%d/"
            return String.format(format, ipv4, port)
        }
    val isReachable: Boolean
        get() {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ipv4, port), 2000)
                    return true
                }
            } catch (e: IOException) {
                return false
            }
        }

    override fun toString(): String {
        return "Server{" +
                "ipv4='" + ipv4 + '\'' +
                ", port=" + port +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val server = other as Server
        return if (port != server.port) false else ipv4 == server.ipv4
    }

    override fun hashCode(): Int {
        var result = ipv4.hashCode()
        result = 31 * result + port
        return result
    }
}