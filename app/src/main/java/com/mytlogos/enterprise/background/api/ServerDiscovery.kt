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
    private val executor = Executors.newFixedThreadPool(maxAddress)

    suspend fun discover(broadcastAddress: InetAddress?): Server? = withContext(Dispatchers.IO) {
        val discoveredServer = Collections.synchronizedSet(HashSet<Server>())
        val futures: MutableList<CompletableFuture<Server?>> = ArrayList()
        for (i in 1 until maxAddress) {
            // this is for emulator sessions,
            // as localhost udp server cannot seem to receive upd packets send from emulator
            futures.add(CompletableFuture.supplyAsync({ discoverLocalNetworkServerPerTcp(i) }, executor))
        }

        var server: Server? = null

        try {
            val future = CompletableFuture.runAsync { discoverLocalNetworkServerPerUdp(broadcastAddress, discoveredServer) }
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
            for (discovered in discoveredServer) {
                if (discovered.isLocal && discovered.isDevServer == isDev) {
                    server = discovered
                    break
                }
            }
            if (server == null && !isDev) {
                server = executor.submit(Callable { discoverInternetServerPerUdp() })[2, TimeUnit.SECONDS]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        executor.shutdownNow()
        return@withContext server
    }

    private fun discoverLocalNetworkServerPerTcp(local: Int): Server? {
        val ipv4 = "192.168.1.$local"
        val server = Server(ipv4, 3000, true, true)
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
            val isDev = devResponse.body() != null && devResponse.body()!!
            return Server(ipv4, 3000, true, isDev)
        } catch (ignored: IOException) {
        }
        return null
    }

    private fun discoverInternetServerPerUdp(): Server? {
        // TODO: 27.07.2019 check if internet server is reachable
        return null
    }

    /**
     * Modified Version from
     * [
 * Network discovery using UDP Broadcast (Java)
](https://michieldemey.be/blog/network-discovery-using-udp-broadcast/) *
     */
    private fun discoverLocalNetworkServerPerUdp(broadcastAddress: InetAddress?, discoveredServer: MutableSet<Server>) {
        // Find the server using UDP broadcast
        //Open a random port to send the package
        try {
            DatagramSocket().use { c ->
                c.broadcast = true
                val sendData = "DISCOVER_SERVER_REQUEST_ENTERPRISE".toByteArray()
                val udpServerPort = 3001
                //Try the some 'normal' ip addresses first
                try {
                    sendUDPPacket(c, sendData, udpServerPort, InetAddress.getLocalHost())
                    sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("255.255.255.255"))
                    sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("192.168.255.255"))
                    for (i in 1..49) {
                        sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("192.168.1.$i"))
                    }
                    if (broadcastAddress != null) {
                        sendUDPPacket(c, sendData, udpServerPort, broadcastAddress)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Broadcast the message over all the network interfaces
                val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
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
                val recvBuf = ByteArray(15000)
                while (true) {
                    val receivePacket = DatagramPacket(recvBuf, recvBuf.size)
                    c.receive(receivePacket)

                    //Check if the message is correct
                    val message = String(receivePacket.data).trim { it <= ' ' }

                    // the weird thing is if the message is over 34 bytes long
                    // e.g. 'DISCOVER_SERVER_RESPONSE_ENTERPRISE' the last character will be cut off
                    // either the node server does not send correctly
                    // or java client does not receive correctly
                    if ("ENTERPRISE_DEV" == message) {
                        val server = Server(receivePacket.address.hostAddress, 3000, true, true)
                        discoveredServer.add(server)
                    } else if ("ENTERPRISE_PROD" == message) {
                        val server = Server(receivePacket.address.hostAddress, 3000, true, false)
                        discoveredServer.add(server)
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun sendUDPPacket(c: DatagramSocket, data: ByteArray, port: Int, address: InetAddress) {
//        System.out.println("Sending Msg to " + address.getHostAddress() + ":" + port);
        c.send(DatagramPacket(data, data.size, address, port))
        c.send(DatagramPacket(data, data.size, address, port))
    }

    fun isReachable(server: Server?): Boolean {
        return server?.isReachable ?: false
    }

    companion object {
        private const val isDev = true
    }
}