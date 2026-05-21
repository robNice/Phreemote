package de.robnice.philipstvcontrol.data.discovery

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.charset.Charset

class SsdpDiscovery(private val context: Context) {

    data class SsdpResponse(val remoteIp: String, val headers: Map<String, String>)

    suspend fun discover(listenMs: Int = 2200): List<SsdpResponse> = withContext(Dispatchers.IO) {
        val multicastLock = acquireMulticastLock()
        Log.d("SSDP", "multicastLock acquired=${multicastLock?.isHeld}")

        try {
            val mSearch = buildMSearch()
            val target = InetAddress.getByName("239.255.255.250")

            // Collect IPv4 addresses from every multicast-capable interface.
            // Binding explicitly per-interface ensures we pick the right outgoing source IP,
            // which the TV needs to send its unicast response back.
            val bindAddrs: List<Inet4Address> = NetworkInterface.getNetworkInterfaces()
                ?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback && it.supportsMulticast() }
                .flatMap { iface ->
                    iface.inetAddresses.toList().filterIsInstance<Inet4Address>().also { addrs ->
                        Log.d("SSDP", "iface=${iface.name} addrs=${addrs.map { it.hostAddress }}")
                    }
                }

            val effectiveAddrs: List<Inet4Address> = bindAddrs.ifEmpty {
                Log.w("SSDP", "No multicast-capable IPv4 interface — falling back to 0.0.0.0")
                @Suppress("UNCHECKED_CAST")
                listOf(InetAddress.getByName("0.0.0.0") as Inet4Address)
            }

            Log.d("SSDP", "Scanning from ${effectiveAddrs.map { it.hostAddress }}")

            coroutineScope {
                effectiveAddrs
                    .map { addr -> async { scanFrom(addr, target, mSearch, listenMs) } }
                    .flatMap { it.await() }
                    .distinctBy { it.remoteIp }
            }
        } finally {
            multicastLock?.let { if (it.isHeld) it.release() }
        }
    }

    private fun scanFrom(
        localAddr: Inet4Address,
        target: InetAddress,
        mSearch: ByteArray,
        listenMs: Int
    ): List<SsdpResponse> {
        val results = mutableListOf<SsdpResponse>()
        val socket = DatagramSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(localAddr, 0))
            soTimeout = 350
            broadcast = true
        }
        Log.d("SSDP", "Socket bound to ${localAddr.hostAddress}:${socket.localPort}")

        try {
            repeat(3) { i ->
                try {
                    socket.send(DatagramPacket(mSearch, mSearch.size, target, 1900))
                    Log.d("SSDP", "M-SEARCH #$i sent from ${localAddr.hostAddress}")
                } catch (e: Exception) {
                    Log.w("SSDP", "M-SEARCH #$i send failed from ${localAddr.hostAddress}: ${e.message}")
                }
                Thread.sleep(120)
            }

            val start = System.currentTimeMillis()
            val buf = ByteArray(4096)
            while (System.currentTimeMillis() - start < listenMs) {
                try {
                    val resp = DatagramPacket(buf, buf.size)
                    socket.receive(resp)
                    val text = String(resp.data, 0, resp.length, Charsets.UTF_8)
                    val ip = resp.address.hostAddress ?: continue
                    Log.d("SSDP", "Response from $ip via ${localAddr.hostAddress}: ${text.take(120).replace("\r\n", " | ")}")
                    results += SsdpResponse(remoteIp = ip, headers = parseSsdpHeaders(text))
                } catch (_: Exception) {
                    // socket timeout between receives — normal
                }
            }
        } finally {
            socket.close()
        }

        Log.d("SSDP", "Interface ${localAddr.hostAddress}: ${results.size} responses")
        return results
    }

    private fun buildMSearch() = buildString {
        append("M-SEARCH * HTTP/1.1\r\n")
        append("HOST: 239.255.255.250:1900\r\n")
        append("MAN: \"ssdp:discover\"\r\n")
        append("MX: 1\r\n")
        append("ST: ssdp:all\r\n")
        append("\r\n")
    }.toByteArray(Charset.forName("UTF-8"))

    private fun acquireMulticastLock(): WifiManager.MulticastLock? {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return null
            wifiManager.createMulticastLock("phreemote:ssdp").apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSsdpHeaders(raw: String): Map<String, String> {
        val lines = raw.split("\r\n", "\n")
        val map = mutableMapOf<String, String>()
        for (line in lines) {
            val idx = line.indexOf(':')
            if (idx > 0) {
                map[line.substring(0, idx).trim().lowercase()] = line.substring(idx + 1).trim()
            }
        }
        return map
    }
}
