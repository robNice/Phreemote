package de.robnice.philipstvcontrol.presentation

sealed class SetupStatus {
    data object Ready : SetupStatus()
    data object Discovering : SetupStatus()
    data class FoundIps(val count: Int) : SetupStatus()
    data object Probing : SetupStatus()
    data class Verified(val count: Int) : SetupStatus()
    data class Error(val reason: String? = null) : SetupStatus()
}