package de.robnice.philipstvcontrol.domain.model

data class FoundTv(
    val ip: String,
    val name: String?,
    val apiMajor: Int?,
    val apiMinor: Int?,
    val basePath: String?,
    val trusted: Boolean,
    val canTrustNow: Boolean,
    val certAvailable: Boolean,
    val fingerprintHex: String?,
    val pinOkHttp: String?
)