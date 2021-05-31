package com.mytlogos.enterprise.background.api;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

class Server {
    private final String ipv4;
    private final int port;
    private final boolean isLocal;
    private final boolean isDevServer;

    Server(String ipv4, int port, boolean isLocal, boolean isDevServer) {
        this.ipv4 = ipv4;
        this.port = port;
        this.isLocal = isLocal;
        this.isDevServer = isDevServer;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public boolean isDevServer() {
        return isDevServer;
    }

    String getIpv4() {
        return ipv4;
    }

    @SuppressLint("DefaultLocale")
    String getAddress() {
        //allow unsafe connections for now in local networks
        String format = "http" + (this.isLocal ? "" : "s") + "://%s:%d/";
        return String.format(format, this.getIpv4(), this.getPort());
    }

    int getPort() {
        return port;
    }

    boolean isReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(getIpv4(), getPort()), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Server{" +
                "ipv4='" + ipv4 + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (getPort() != server.getPort()) return false;
        return getIpv4().equals(server.getIpv4());
    }

    @Override
    public int hashCode() {
        int result = getIpv4().hashCode();
        result = 31 * result + getPort();
        return result;
    }
}
