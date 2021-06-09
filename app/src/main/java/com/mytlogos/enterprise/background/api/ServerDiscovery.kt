package com.mytlogos.enterprise.background.api

import com.google.gson.GsonBuilder
import com.mytlogos.enterprise.background.api.GsonAdapter.DateTimeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.*

internal class ServerDiscovery {
    private val maxAddress = 50
    private val executor = Executors.newFixedThreadPool(maxAddress, object : ThreadFactory {
        var id = 0
        override fun newThread(r: Runnable?) = Thread(r, "Server-Discovery-TCP-${id++}")
    })

    suspend fun discover(broadcastAddress: InetAddress?): Server? = withContext(Dispatchers.IO) {
        val discoveredServer = Collections.synchronizedSet(HashSet<Server>())
        val futures: MutableList<CompletableFuture<Server?>> = ArrayList()

        for (i in 1 until maxAddress) {
            // this is for emulator sessions,
            // as localhost udp server cannot seem to receive upd packets send from emulator
            futures.add(CompletableFuture.supplyAsync({
                return@supplyAsync try {
                    println("${Thread.currentThread()} Started TCP Discovery of $i")
                    discoverLocalNetworkServerPerTcp(i)
                } finally {
                    println("${Thread.currentThread()} Finished TCP Discovery of $i")
                }
            }, executor))
        }

        var server: Server? = null

        val udpExecutor = Executors.newSingleThreadExecutor { Thread(it, "Server-Discovery-UDP") }
        try {
            val future = CompletableFuture.runAsync({
                println("${Thread.currentThread()} Started UDP Discovery")
                discoverLocalNetworkServerPerUdp(broadcastAddress, discoveredServer)
            }, udpExecutor)
            try {
                future[5, TimeUnit.SECONDS]
            } catch (ignored: TimeoutException) {
            }
            for (serverFuture in futures) {
                try {
                    val now = serverFuture.getNow(null)
                    if (now != null) {
                        discoveredServer.add(now)
                    } else if (!serverFuture.isDone) {
                        serverFuture.cancel(true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            println("${Thread.currentThread()} Total discovered Server: ${discoveredServer.size}")
            server = discoveredServer.find { it.isLocal && it.isDevServer == isDev }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        executor.shutdownNow()
        udpExecutor.shutdownNow()
        println("${Thread.currentThread()} Discovered Server: $server")
        return@withContext server
    }

    private fun discoverLocalNetworkServerPerTcp(local: Int): Server? {
        val ipv4 = "192.168.1.$local"
        val server = Server(
            ipv4,
            3000,
            isLocal = true,
            isDevServer = true
        )

        if (!server.isReachable) {
            return null
        }
        try {
            val gson = GsonBuilder()
                .registerTypeHierarchyAdapter(DateTime::class.java, DateTimeAdapter())
                .create()

            val client = OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(server.address)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiImpl = retrofit.create(BasicApi::class.java)
            val devResponse = apiImpl.checkDev("api").execute()
            val isDev = devResponse.body() ?: return null

            return Server(ipv4, 3000, true, isDev)
        } catch (ignored: IOException) {
        }
        return null
    }

    /**
     * Modified Version from
     * [
 * Network discovery using UDP Broadcast (Java)
](https://michieldemey.be/blog/network-discovery-using-udp-broadcast/) *
     */
    private fun discoverLocalNetworkServerPerUdp(
        broadcastAddress: InetAddress?,
        discoveredServer: MutableSet<Server>,
    ) {
        // Find the server using UDP broadcast
        //Open a random port to send the package
        try {
            DatagramChannel.open().use { c ->
                c.setOption(StandardSocketOptions.SO_BROADCAST, true)
                val sendData = "DISCOVER_SERVER_REQUEST_ENTERPRISE".toByteArray()
                val udpServerPort = 3001
                //Try the some 'normal' ip addresses first
                try {
                    sendUDPPacket(c, sendData, udpServerPort, InetAddress.getLocalHost())
                    sendUDPPacket(c,
                        sendData,
                        udpServerPort,
                        InetAddress.getByName("255.255.255.255"))
                    sendUDPPacket(c,
                        sendData,
                        udpServerPort,
                        InetAddress.getByName("192.168.255.255"))
                    for (i in 1..49) {
                        sendUDPPacket(c,
                            sendData,
                            udpServerPort,
                            InetAddress.getByName("192.168.1.$i"))
                    }
                    if (broadcastAddress != null) {
                        sendUDPPacket(c, sendData, udpServerPort, broadcastAddress)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Broadcast the message over all the network interfaces
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (networkInterface in interfaces) {
                    if (!networkInterface.isUp) {
                        continue  // Don't want to broadcast to the loopback interface
                    }
                    for (interfaceAddress in networkInterface.interfaceAddresses) {
                        val broadcast = interfaceAddress.broadcast ?: continue

                        // Send the broadcast package!
                        try {
                            sendUDPPacket(c, sendData, udpServerPort, broadcast)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                //Wait for a response
                val recvBuf = ByteBuffer.allocate(15000)
                val serverId = udpServerId.incrementAndGet()

                while (true) {
                    val sender = c.receive(recvBuf)

                    if (sender !is InetSocketAddress) {
                        continue
                    }

                    //Check if the message is correct
                    val message = String(recvBuf.array()).trim { it <= ' ' }

                    // the weird thing is if the message is over 34 bytes long
                    // e.g. 'DISCOVER_SERVER_RESPONSE_ENTERPRISE' the last character will be cut off
                    // either the node server does not send correctly
                    // or java client does not receive correctly
                    if ("ENTERPRISE_DEV" == message) {
                        val server = Server(
                            sender.hostName,
                            3000,
                            isLocal = true,
                            isDevServer = true
                        )
                        discoveredServer.add(server)
                    } else if ("ENTERPRISE_PROD" == message) {
                        val server = Server(
                            sender.hostName,
                            3000,
                            isLocal = true,
                            isDevServer = false
                        )
                        discoveredServer.add(server)
                    }
                }
            }
        } catch (ex: IOException) {
            if (ex !is ClosedByInterruptException) {
                ex.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun sendUDPPacket(c: DatagramChannel, data: ByteArray, port: Int, address: InetAddress) {
//        System.out.println("Sending Msg to " + address.getHostAddress() + ":" + port);
        c.send(ByteBuffer.wrap(data), InetSocketAddress(address, port))
        c.send(ByteBuffer.wrap(data), InetSocketAddress(address, port))
    }

    fun isReachable(server: Server?): Boolean {
        return server?.isReachable ?: false
    }

    companion object {
        private const val isDev = true
        private val tcpId = AtomicInteger()
        private val udpId = AtomicInteger()
        private val udpServerId = AtomicInteger()
    }
}