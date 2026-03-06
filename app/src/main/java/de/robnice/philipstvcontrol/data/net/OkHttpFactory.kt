package de.robnice.philipstvcontrol.data.net

import android.annotation.SuppressLint
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class OkHttpFactory {

    fun pinnedClient(host: String, pin: String): OkHttpClient {
        val pinner = CertificatePinner.Builder()
            .add(host, pin)
            .build()

        return OkHttpClient.Builder()
            .certificatePinner(pinner)
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }


    fun unsafeProbeClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        )
        val tm = trustAll[0] as X509TrustManager
        val ssl = SSLContext.getInstance("TLS")
        ssl.init(null, trustAll, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(ssl.socketFactory, tm)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }

    fun tofuPinnedClient(host: String, sha256Pin: String): OkHttpClient {
        val trustAllManager = object : javax.net.ssl.X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }

        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<javax.net.ssl.TrustManager>(trustAllManager), java.security.SecureRandom())

        val pinner = okhttp3.CertificatePinner.Builder()
            .add(host, sha256Pin)
            .build()

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllManager)
            .hostnameVerifier { _, _ -> true }
            .certificatePinner(pinner)
            .build()
    }
}