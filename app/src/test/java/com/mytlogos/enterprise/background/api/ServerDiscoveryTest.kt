package com.mytlogos.enterprise.background.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

class ServerDiscoveryTest {

    private final ServerDiscovery discovery = new ServerDiscovery();

    @Test
    void discover() {
        // for this test to not fail with an exception, a server needs to be running on this network
        Server server = null;
        try {
            server = discovery.discover(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Assertions.assertNotNull(server);
    }
}