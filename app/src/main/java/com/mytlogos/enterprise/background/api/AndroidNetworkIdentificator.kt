package com.mytlogos.enterprise.background.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.UnknownHostException

class AndroidNetworkIdentificator(private val context: Context) : NetworkIdentificator {
    override val sSID: String
        get() {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo == null || !networkInfo.isConnected) {
                return ""
            }
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            if (connectionInfo == null || connectionInfo.ssid.trim { it <= ' ' }.isEmpty()) {
                return ""
            }
            val ssid = connectionInfo.ssid
            return if ("<unknown ssid>" == ssid) {
                ""
            } else ssid
        }// handle null somehow

    /**
     * Courtesy to
     * [
 * Send Broadcast UDP but not receive it on other android devices
](https://stackoverflow.com/a/25520279) *
     *
     * @return BroadcastAddress
     */
    override val broadcastAddress: InetAddress?
        get() {
            val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp = wifi.dhcpInfo ?: return null
            // handle null somehow
            val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
            val quads = ByteArray(4)
            quads[0] = (broadcast and 0xFF).toByte()
            quads[1] = (broadcast shr 8 and 0xFF).toByte()
            quads[2] = (broadcast shr 2 * 8 and 0xFF).toByte()
            quads[3] = (broadcast shr 3 * 8 and 0xFF).toByte()
            return try {
                InetAddress.getByAddress(quads)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                null
            }
        }
}