package de.robnice.philipstvcontrol.data.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.Charset

class SsdpDiscovery {

    data class SsdpResponse(
        val remoteIp: String,
        val headers: Map<String, String>
    )

    /**
     * Sends SSDP M-SEARCH and collects responses for [listenMs].
     * Returns raw SSDP responses with headers + responder IP.
     */
    suspend fun discover(listenMs: Int = 1200): List<SsdpResponse> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SsdpResponse>()
        val socket = DatagramSocket().apply {
            soTimeout = 250
            broadcast = true
        }

        val mSearch = buildString {
            append("M-SEARCH * HTTP/1.1\r\n")
            append("HOST: 239.255.255.250:1900\r\n")
            append("MAN: \"ssdp:discover\"\r\n")
            append("MX: 1\r\n")
            append("ST: ssdp:all\r\n")
            append("\r\n")
        }.toByteArray(Charset.forName("UTF-8"))

        val addr = InetAddress.getByName("239.255.255.250")
        val packet = DatagramPacket(mSearch, mSearch.size, addr, 1900)
        socket.send(packet)

        val start = System.currentTimeMillis()
        val buf = ByteArray(4096)

        while (System.currentTimeMillis() - start < listenMs) {
            try {
                val resp = DatagramPacket(buf, buf.size)
                socket.receive(resp)

                val text = String(resp.data, 0, resp.length, Charsets.UTF_8)
                val headers = parseSsdpHeaders(text)
                val ip = resp.address.hostAddress

                results += SsdpResponse(remoteIp = ip, headers = headers)
            } catch (_: Exception) {
                // Do nothing at this point? I guess. Will see if I need to do something here later.
                // Do I look like I don't know what I'm doing?
                // I call this a life style.
                // Also you are right.
            }
        }

        socket.close()
        results.distinctBy { it.remoteIp }
    }

    private fun parseSsdpHeaders(raw: String): Map<String, String> {
        val lines = raw.split("\r\n", "\n")
        val map = mutableMapOf<String, String>()
        for (line in lines) {
            val idx = line.indexOf(':')
            if (idx > 0) {
                val key = line.substring(0, idx).trim().lowercase()
                val value = line.substring(idx + 1).trim()
                map[key] = value
            }
        }
        return map
    }
}