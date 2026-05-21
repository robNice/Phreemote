package de.robnice.philipstvcontrol.data.tv

import de.robnice.philipstvcontrol.domain.model.TvCandidate

sealed class ProbeResult {
    data class Verified(val tv: TvCandidate) : ProbeResult()
    data class Untrusted(
        val ip: String,
        val httpsPort: Int,
        val displayName: String?,
        val apiMajor: Int?,
        val apiMinor: Int?,
        val basePath: String?,
        val pinOkHttp: String,
        val fingerprintHex: String,
        val certAvailable: Boolean = true
    ) : ProbeResult()

    data object NotATv : ProbeResult()
}