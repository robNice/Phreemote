package de.robnice.philipstvcontrol.domain.model

data class TvCandidate(
    val ip: String,
    val displayName: String? = null,
    val httpsPort: Int? = null,
    val httpPort: Int? = null,
    val apiMajor: Int? = null,
    val apiMinor: Int? = null,
    val basePath: String? = null,
    val verified: Boolean = false,
    val source: String = "SSDP"
)