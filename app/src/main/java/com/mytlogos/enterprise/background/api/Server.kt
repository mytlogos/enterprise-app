package com.mytlogos.enterprise.background.api

import android.net.Uri
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal data class Server(
    val ipv4: String,
    val port: Int,
    val isLocal: Boolean,
    val isDevServer: Boolean,
) {

    //allow unsafe connections for now in local networks
    val address: String
        get() {
            //allow unsafe connections for now in local networks
            return "http${if (isLocal) "" else "s"}://$ipv4:$port/"
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val server = other as Server
        return port == server.port && ipv4 == server.ipv4
    }

    override fun hashCode(): Int {
        var result = ipv4.hashCode()
        result = 31 * result + port
        return result
    }

    companion object {
        fun fromString(url: String): Server {
            val uri = Uri.parse(url)
            var port = uri.port

            if (port < 0) {
                port = if (uri.scheme == "https") 443 else 80
            }
            return Server(ipv4 = uri.host!!, port = port, isLocal = true, false)
        }
    }
}