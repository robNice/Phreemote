package de.robnice.philipstvcontrol.data.tv

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class DigestAuthenticator(
    private val user: String,
    private val pass: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) return null

        val www = response.header("WWW-Authenticate") ?: return null
        if (!www.startsWith("Digest", ignoreCase = true)) return null

        val digest = DigestHeader.parse(www) ?: return null
        val method = response.request.method
        val uri = response.request.url.encodedPath

        val auth = digest.buildAuthorization(user, pass, method, uri)
        return response.request.newBuilder()
            .header("Authorization", auth)
            .build()
    }

    private fun responseCount(resp: Response): Int {
        var r: Response? = resp
        var count = 1
        while ((r?.priorResponse) != null) {
            count++
            r = r.priorResponse
        }
        return count
    }
}