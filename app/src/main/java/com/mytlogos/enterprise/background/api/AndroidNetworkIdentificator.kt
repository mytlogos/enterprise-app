package com.mytlogos.enterprise.background.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class AndroidNetworkIdentificator implements NetworkIdentificator {
    private final Context context;

    public AndroidNetworkIdentificator(Context context) {
        this.context = context;
    }

    @Override
    public String getSSID() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return "";
        }

        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        if (connectionInfo == null || connectionInfo.getSSID().trim().isEmpty()) {
            return "";
        }
        String ssid = connectionInfo.getSSID();

        if ("<unknown ssid>".equals(ssid)) {
            return "";
        }
        return ssid;
    }

    /**
     * Courtesy to
     * <a href="https://stackoverflow.com/a/25520279">
     * Send Broadcast UDP but not receive it on other android devices
     * </a>
     *
     * @return BroadcastAddress
     */
    @Override
    public InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        if (dhcp == null) {
            return null;
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        quads[0] = (byte) (broadcast & 0xFF);
        quads[1] = (byte) ((broadcast >> 8) & 0xFF);
        quads[2] = (byte) ((broadcast >> 2 * 8) & 0xFF);
        quads[3] = (byte) ((broadcast >> 3 * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
