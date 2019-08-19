package com.mytlogos.enterprise.background.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ServerDiscovery {

    private final int maxAddress = 50;
    private final ExecutorService executor = Executors.newFixedThreadPool(maxAddress);
    private static final boolean isDev = true;


    Server discover(InetAddress broadcastAddress) {
        Set<Server> discoveredServer = Collections.synchronizedSet(new HashSet<>());

        List<CompletableFuture<Server>> futures = new ArrayList<>();
        for (int i = 1; i < maxAddress; i++) {
            int local = i;
            // this is for emulator sessions,
            // as localhost udp server cannot seem to receive upd packets send from emulator
            futures.add(CompletableFuture.supplyAsync(() -> this.discoverLocalNetworkServerPerTcp(local), executor));
        }
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> this.discoverLocalNetworkServerPerUdp(broadcastAddress, discoveredServer));
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException ignored) {
            }

            for (CompletableFuture<Server> serverFuture : futures) {
                try {
                    Server now = serverFuture.getNow(null);
                    if (now != null) {
                        discoveredServer.add(now);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Server server = null;

            for (Server discovered : discoveredServer) {
                if (discovered.isLocal() && discovered.isDevServer() == isDev) {
                    server = discovered;
                    break;
                }
            }
            if (server != null) {
                return server;
            }
            return executor.submit(this::discoverInternetServerPerUdp).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    private Server discoverLocalNetworkServerPerTcp(int local) {
        // it is unknown if any tcp discovered server are dev or not, assume for now they are
        Server server = new Server("192.168.1." + local, 3000, true, true);
        return this.isReachable(server) ? server : null;
    }

    private Server discoverInternetServerPerUdp() {
        // TODO: 27.07.2019 check if internet server is reachable
        return null;
    }

    /**
     * Modified Version from
     * <a href="https://michieldemey.be/blog/network-discovery-using-udp-broadcast/">
     * Network discovery using UDP Broadcast (Java)
     * </a>
     */
    private void discoverLocalNetworkServerPerUdp(InetAddress broadcastAddress, Set<Server> discoveredServer) {
        // Find the server using UDP broadcast
        //Open a random port to send the package
        try (DatagramSocket c = new DatagramSocket()) {
            c.setBroadcast(true);

            byte[] sendData = "DISCOVER_SERVER_REQUEST_ENTERPRISE".getBytes();

            int udpServerPort = 3001;
            //Try the some 'normal' ip addresses first
            try {
                this.sendUDPPacket(c, sendData, udpServerPort, InetAddress.getLocalHost());
                this.sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("255.255.255.255"));
                this.sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("192.168.255.255"));

                for (int i = 1; i < 50; i++) {
                    this.sendUDPPacket(c, sendData, udpServerPort, InetAddress.getByName("192.168.1." + i));
                }

                if (broadcastAddress != null) {
                    this.sendUDPPacket(c, sendData, udpServerPort, broadcastAddress);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Broadcast the message over all the network interfaces
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaces) {
                if (!networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        sendUDPPacket(c, sendData, udpServerPort, broadcast);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            //Wait for a response
            byte[] recvBuf = new byte[15000];

            //noinspection InfiniteLoopStatement
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);

                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                // the weird thing is if the message is over 34 bytes long
                // e.g. 'DISCOVER_SERVER_RESPONSE_ENTERPRISE' the last character will be cut off
                // either the node server does not send correctly
                // or java client does not receive correctly
                if ("ENTERPRISE_DEV".equals(message)) {
                    Server server = new Server(receivePacket.getAddress().getHostAddress(), 3000, true, true);
                    discoveredServer.add(server);
                } else if ("ENTERPRISE_PROD".equals(message)) {
                    Server server = new Server(receivePacket.getAddress().getHostAddress(), 3000, true, false);
                    discoveredServer.add(server);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendUDPPacket(DatagramSocket c, byte[] data, int port, InetAddress address) throws IOException {
        System.out.println("Sending Msg to " + address.getHostAddress() + ":" + port);
        c.send(new DatagramPacket(data, data.length, address, port));
        c.send(new DatagramPacket(data, data.length, address, port));
    }

    boolean isReachable(Server server) {
        if (server == null) {
            return false;
        }
        return server.isReachable();
    }
}
