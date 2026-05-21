package de.robnice.philipstvcontrol.data.tv

import android.util.Log
import de.robnice.philipstvcontrol.data.net.OkHttpFactory
import de.robnice.philipstvcontrol.data.trust.CertPins
import de.robnice.philipstvcontrol.data.trust.DEV_TRUST_ALL
import de.robnice.philipstvcontrol.domain.model.TvCandidate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TvSystemProbe(
    private val okHttpFactory: OkHttpFactory
) {

    fun probeTrusted(ip: String, pin: String): ProbeResult {
        val port = 1926

        return try {
            val client =
                if (pin == DEV_TRUST_ALL)
                    okHttpFactory.unsafeProbeClient()
                else
                    okHttpFactory.pinnedClient(ip, pin)

            val sys = getJson(client, "https://$ip:$port/system")
                ?: return ProbeResult.NotATv

            ProbeResult.Verified(buildCandidate(ip, port, sys))

        } catch (e: Exception) {

            try {
                val fallbackClient = okHttpFactory.unsafeProbeClient()
                val sys = getJson(fallbackClient, "https://$ip:$port/system")
                    ?: return ProbeResult.NotATv

                ProbeResult.Verified(buildCandidate(ip, port, sys))
            } catch (_: Exception) {
                ProbeResult.NotATv
            }
        }
    }

    fun probeUntrusted(ip: String): ProbeResult {
        val port = 1926

        val client = okHttpFactory.unsafeProbeClient()

        return try {
            val req = Request.Builder()
                .url("https://$ip:$port/system")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty().trim()

                Log.d("TV", "HTTPS $ip:$port/system -> code=${resp.code} len=${body.length} head=${body.take(80)}")
                if (!resp.isSuccessful) return ProbeResult.NotATv
                if (body.isEmpty()) return ProbeResult.NotATv

                val json = try { JSONObject(body) } catch (_: Exception) { return ProbeResult.NotATv }
                val fields = parseSystemFields(json)

                val handshake = resp.handshake
                val peerAny = handshake?.peerCertificates?.firstOrNull()

                Log.d("TV", "Handshake present=${handshake != null}")
                Log.d("TV", "Peer cert from handshake present=${peerAny != null} class=${peerAny?.javaClass?.name}")

                val peerCert: X509Certificate? = when {
                    peerAny is X509Certificate -> peerAny
                    peerAny != null -> {
                        try {
                            val cf = CertificateFactory.getInstance("X.509")
                            cf.generateCertificate(ByteArrayInputStream(peerAny.encoded)) as X509Certificate
                        } catch (e: Exception) {
                            Log.d("TV", "Peer cert conversion failed", e)
                            null
                        }
                    }
                    else -> null
                } ?: fetchPeerCertViaSslSocket(ip, port)

                if (peerCert == null) {
                    return ProbeResult.Untrusted(
                        ip = ip,
                        httpsPort = port,
                        displayName = fields.name,
                        apiMajor = fields.major,
                        apiMinor = fields.minor,
                        basePath = fields.basePath,
                        pinOkHttp = "",
                        fingerprintHex = "",
                        certAvailable = false
                    )
                }

                val pin = CertPins.okhttpSha256Pin(peerCert)
                val hex = CertPins.prettyHexFingerprint(peerCert)

                ProbeResult.Untrusted(
                    ip = ip,
                    httpsPort = port,
                    displayName = fields.name,
                    apiMajor = fields.major,
                    apiMinor = fields.minor,
                    basePath = fields.basePath,
                    pinOkHttp = pin,
                    fingerprintHex = hex,
                    certAvailable = true
                )
            }
        } catch (e: Exception) {
            Log.d("TV", "Probe exception for $ip", e)
            ProbeResult.NotATv
        }
    }

    private data class ParsedFields(
        val name: String?,
        val major: Int?,
        val minor: Int?,
        val basePath: String?
    )

    private fun parseSystemFields(sys: JSONObject): ParsedFields {
        val api = sys.optJSONObject("api_version")
        val major = api?.optInt("Major")?.takeIf { it > 0 }
        val minor = api?.optInt("Minor")?.takeIf { it >= 0 }
        val basePath = major?.let { "/$it" }

        val rawName = sys.optString("name", "")
        val name = rawName.takeIf { it.isNotBlank() }

        return ParsedFields(name = name, major = major, minor = minor, basePath = basePath)
    }

    private fun buildCandidate(ip: String, port: Int, sys: JSONObject): TvCandidate {
        val fields = parseSystemFields(sys)

        return TvCandidate(
            ip = ip,
            displayName = fields.name,
            httpsPort = port,
            apiMajor = fields.major,
            apiMinor = fields.minor,
            basePath = fields.basePath,
            verified = true,
            source = "SSDP+TOFU"
        )
    }

    private fun getJson(client: OkHttpClient, url: String): JSONObject? {
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string()?.trim().orEmpty()
            if (body.isEmpty()) return null
            return try { JSONObject(body) } catch (_: Exception) { null }
        }
    }

    /**
     * Fallback: hole das Leaf-Cert per eigener TLS-Handshake-Session.
     * Wichtig: TrustAll + Hostname ignorieren, sonst scheitert es auf echten TVs häufig.
     */
    private fun fetchPeerCertViaSslSocket(ip: String, port: Int): X509Certificate? {
        return try {
            val trustAll = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                }
            )

            val ctx = SSLContext.getInstance("TLS")
            ctx.init(null, trustAll, SecureRandom())

            (ctx.socketFactory.createSocket(ip, port) as SSLSocket).use { socket ->
                socket.soTimeout = 2500

                val params = socket.sslParameters
                params.endpointIdentificationAlgorithm = null
                socket.sslParameters = params

                socket.startHandshake()
                val certs = socket.session.peerCertificates
                certs.firstOrNull() as? X509Certificate
            }
        } catch (e: Exception) {
            Log.d("TV", "SSLSocket cert fetch failed for $ip:$port", e)
            null
        }
    }
}