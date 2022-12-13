package com.mytlogos.enterprise.background.api

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.UnknownHostException

class ServerDiscoveryTest {
    private val discovery = ServerDiscovery()

    @Test
    fun discover() {
        // for this test to not fail with an exception, a server needs to be running on this network
        var server: List<Server> = listOf()
        runBlocking {
            try {
                server = discovery.discover(InetAddress.getLocalHost())
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
        Assertions.assertEquals(server.size, 0)
    }
}
