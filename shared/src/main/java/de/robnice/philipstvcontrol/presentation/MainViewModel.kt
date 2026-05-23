package de.robnice.philipstvcontrol.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.robnice.philipstvcontrol.data.discovery.SsdpDiscovery
import de.robnice.philipstvcontrol.data.net.OkHttpFactory
import de.robnice.philipstvcontrol.data.trust.TvTrustStore
import de.robnice.philipstvcontrol.data.tv.ProbeResult
import de.robnice.philipstvcontrol.data.tv.TvSystemProbe
import de.robnice.philipstvcontrol.domain.model.TvCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import de.robnice.philipstvcontrol.data.platform.Platform
import de.robnice.philipstvcontrol.data.trust.DEV_TRUST_ALL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import de.robnice.philipstvcontrol.domain.model.FoundTv
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import de.robnice.philipstvcontrol.data.settings.CustomButtonStore
import de.robnice.philipstvcontrol.data.settings.TvSelectionStore
import de.robnice.philipstvcontrol.data.tv.PhilipsPairingService
import de.robnice.philipstvcontrol.domain.model.CUSTOM_BUTTON_COUNT
import de.robnice.philipstvcontrol.domain.model.CustomButton
import de.robnice.philipstvcontrol.domain.model.RemoteAction
import de.robnice.philipstvcontrol.data.tv.PhilipsRemoteService

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val discovery = SsdpDiscovery(app.applicationContext)
    private val okHttpFactory = OkHttpFactory()
    private val probe = TvSystemProbe(okHttpFactory)
    private val remoteService = PhilipsRemoteService(okHttpFactory)
    private val trustStore = TvTrustStore(app.applicationContext)
    private val selectionStore = TvSelectionStore(app.applicationContext)
    private val remoteMutex = kotlinx.coroutines.sync.Mutex()

    private val _candidates = MutableStateFlow<List<TvCandidate>>(emptyList())
    val candidates: StateFlow<List<TvCandidate>> = _candidates

    private val _status = MutableStateFlow<SetupStatus>(SetupStatus.Ready)
    val status: StateFlow<SetupStatus> = _status

    private var scanJob: Job? = null

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _foundTvs = MutableStateFlow<List<FoundTv>>(emptyList())
    val foundTvs: StateFlow<List<FoundTv>> = _foundTvs

    private val _selectedIp = MutableStateFlow<String?>(null)
    val selectedIp: StateFlow<String?> = _selectedIp

    private val _selectedBasePath = MutableStateFlow<String?>(null)
    val selectedBasePath: StateFlow<String?> = _selectedBasePath

    private val _selectedPaired = MutableStateFlow(false)
    val selectedPaired: StateFlow<Boolean> = _selectedPaired

    private val _bootstrapped = MutableStateFlow(false)
    val bootstrapped: StateFlow<Boolean> = _bootstrapped

    private val _tvOnline = MutableStateFlow<Boolean?>(null)
    val tvOnline: StateFlow<Boolean?> = _tvOnline

    private val _selectedDigestUser = MutableStateFlow<String?>(null)
    val selectedDigestUser: StateFlow<String?> = _selectedDigestUser

    private val _selectedDigestPass = MutableStateFlow<String?>(null)
    val selectedDigestPass: StateFlow<String?> = _selectedDigestPass

    private val customButtonStore = CustomButtonStore(app.applicationContext)

    private val _customButtons = MutableStateFlow<List<CustomButton>>(
        List(CUSTOM_BUTTON_COUNT) { CustomButton() }
    )
    val customButtons: StateFlow<List<CustomButton>> = _customButtons

    private val pairingService = PhilipsPairingService(okHttpFactory)

    private val _pairingRequest = MutableStateFlow<PhilipsPairingService.PairRequestResult?>(null)
    val pairingRequest: StateFlow<PhilipsPairingService.PairRequestResult?> = _pairingRequest

    private val _pairingError = MutableStateFlow<String?>(null)
    val pairingError: StateFlow<String?> = _pairingError

    private val _showSetup = MutableStateFlow(false)
    val showSetup: StateFlow<Boolean> = _showSetup

    private val _demoMode = MutableStateFlow(false)
    val demoMode: StateFlow<Boolean> = _demoMode

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedIp.value = selectionStore.getSelectedIp()
            _selectedBasePath.value = selectionStore.getSelectedBasePath()
            _selectedPaired.value = selectionStore.getSelectedPaired()
            _selectedDigestUser.value = selectionStore.getSelectedDigestUser()
            _selectedDigestPass.value = selectionStore.getSelectedDigestPass()
            _customButtons.value = customButtonStore.getAll()
            _bootstrapped.value = true

            val ip = _selectedIp.value
            val pin = ip?.let { trustStore.getPin(it) }
            if (ip != null && !pin.isNullOrBlank() && _selectedPaired.value) {
                _tvOnline.value = probe.probeTrusted(ip, pin) is ProbeResult.Verified
            }
        }
    }

    fun runDiscovery() {
        scanJob?.cancel()

        scanJob = viewModelScope.launch {
            _isScanning.value = true
            _foundTvs.value = emptyList()

            try {
                _status.value = SetupStatus.Discovering

                val (ssdp, subnetIps) = withContext(Dispatchers.IO) {
                    coroutineScope {
                        val ssdpJob = async {
                            try { discovery.discover() }
                            catch (e: Exception) {
                                Log.w("TV", "SSDP failed: ${e.message}")
                                emptyList<SsdpDiscovery.SsdpResponse>()
                            }
                        }
                        val subnetJob = async { subnetScan() }
                        ssdpJob.await() to subnetJob.await()
                    }
                }

                Log.d("TV", "SSDP found ${ssdp.size}, subnet scan found ${subnetIps.size} candidates")

                _status.value = SetupStatus.Probing

                val found = withContext(Dispatchers.IO) {
                    val results = mutableListOf<FoundTv>()
                    val ipsToProbe = LinkedHashSet<String>()

                    for (resp in ssdp) {
                        ipsToProbe += resp.remoteIp
                    }
                    for (ip in subnetIps) {
                        ipsToProbe += ip
                    }

                    _selectedIp.value?.let { ipsToProbe += it }

                    for (ip in ipsToProbe) {
                        if (!isActive) break
                        val pin = trustStore.getPin(ip)
                        val trusted = !pin.isNullOrBlank()

                        Log.d("TV", "Probing IP=$ip pin=$pin trusted=$trusted")

                        val probeResult = if (trusted) {
                            probe.probeTrusted(ip, pin!!)
                        } else {
                            probe.probeUntrusted(ip)
                        }

                        Log.d("TV", "Probe result for $ip: ${probeResult::class.simpleName}")

                        when (probeResult) {
                            is ProbeResult.Verified -> {
                                val tv = probeResult.tv
                                results += FoundTv(
                                    ip = tv.ip,
                                    name = tv.displayName,
                                    apiMajor = tv.apiMajor,
                                    apiMinor = tv.apiMinor,
                                    basePath = tv.basePath,
                                    trusted = true,
                                    canTrustNow = true,
                                    certAvailable = true,
                                    fingerprintHex = null,
                                    pinOkHttp = null
                                )
                            }

                            is ProbeResult.Untrusted -> {
                                Log.d("TV", "Platform.isEmulator = ${Platform.isEmulator}")
                                val canTrust = probeResult.certAvailable || Platform.isEmulator
                                results += FoundTv(
                                    ip = probeResult.ip,
                                    name = probeResult.displayName,
                                    apiMajor = probeResult.apiMajor,
                                    apiMinor = probeResult.apiMinor,
                                    basePath = probeResult.basePath,
                                    trusted = false,
                                    canTrustNow = canTrust,
                                    certAvailable = probeResult.certAvailable,
                                    fingerprintHex = probeResult.fingerprintHex,
                                    pinOkHttp = probeResult.pinOkHttp
                                )
                            }

                            ProbeResult.NotATv -> Unit
                        }
                    }

                    results.sortedWith(
                        compareByDescending<FoundTv> { it.trusted }
                            .thenBy { it.name ?: it.ip }
                    )
                }

                _foundTvs.value = found
                _status.value = SetupStatus.Verified(found.count { it.trusted })

            } catch (e: Exception) {
                Log.e("TV", "Discovery failed", e)
                _status.value = SetupStatus.Error(e.message)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun connectTo(tv: FoundTv) {
        viewModelScope.launch {

            Log.d("TV", "connectTo: ip=${tv.ip} trusted=${tv.trusted} certAvailable=${tv.certAvailable} canTrustNow=${tv.canTrustNow} emu=${Platform.isEmulator}")

            if (!tv.trusted && !tv.canTrustNow) {
                Log.e("TV", "connectTo blocked: no cert available on real device -> cannot TOFU-pin")
            }


            if (tv.trusted) {
                _selectedIp.value = tv.ip
                _selectedBasePath.value = tv.basePath
                _selectedPaired.value = false
                _tvOnline.value = null
                selectionStore.setSelectedTv(tv.ip, tv.basePath, false)
                _showSetup.value = false
                return@launch
            }

            val pinToStore =
                if (!tv.certAvailable && Platform.isEmulator) DEV_TRUST_ALL
                else tv.pinOkHttp.orEmpty()

            if (!tv.certAvailable && !Platform.isEmulator) return@launch

            if (!Platform.isEmulator && pinToStore.isBlank()) return@launch

            Log.d("TV", "Connect: storing pin=$pinToStore for ip=${tv.ip}")
            trustStore.setPin(tv.ip, pinToStore)

            _selectedIp.value = tv.ip
            _selectedBasePath.value = tv.basePath
            _selectedPaired.value = false
            _tvOnline.value = null
            selectionStore.setSelectedTv(tv.ip, tv.basePath, false)
            _showSetup.value = false

            runDiscovery()
        }
    }

    fun cancelDiscovery() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    /*fun clearSelectedTv() {
        viewModelScope.launch {
            selectionStore.clearSelectedTv()
            _selectedIp.value = null
            _selectedBasePath.value = null
            _selectedPaired.value = false
        }
    }*/

    fun openSetup() {
        _showSetup.value = true
    }

    fun closeSetup() {
        _showSetup.value = false
    }

    fun enterDemoMode() {
        _demoMode.value = true
        _showSetup.value = false
    }

    fun exitDemoMode() {
        _demoMode.value = false
    }

    fun forgetTvCompletely() {
        viewModelScope.launch {
            val ip = _selectedIp.value
            selectionStore.forgetSelectedTvCompletely()
            if (ip != null) withContext(Dispatchers.IO) { trustStore.clearPin(ip) }
            _foundTvs.value = emptyList()
            _selectedIp.value = null
            _selectedBasePath.value = null
            _selectedPaired.value = false
            _selectedDigestUser.value = null
            _selectedDigestPass.value = null
            _tvOnline.value = null
        }
    }


    fun startPairing() {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = _selectedIp.value ?: return@launch
            val basePath = _selectedBasePath.value
            val tlsMarker = trustStore.getPin(ip) ?: DEV_TRUST_ALL

            try {
                Log.d("TV", "Pairing start: ip=$ip basePath=$basePath tls=$tlsMarker")
                val req = pairingService.startPairing(ip, tlsMarker, basePath)
                Log.d("TV", "Pairing request OK: deviceId=${req.deviceId} ts=${req.timestamp}")


                _pairingRequest.value = req
                _pairingError.value = null
            } catch (e: Exception) {
                Log.e("TV", "Pairing request FAILED", e)
                _pairingError.value = e.message
            }
        }
    }

    fun submitPin(pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = _selectedIp.value ?: return@launch
            val basePath = _selectedBasePath.value
            val tlsMarker = trustStore.getPin(ip) ?: DEV_TRUST_ALL
            val req = _pairingRequest.value ?: return@launch

            try {
                val ok = pairingService.grantPairing(ip, tlsMarker, basePath, req, pin)
                if (ok) {
                    Log.d("TV", "Pairing OK -> setting selectedPaired=true")
                    selectionStore.setPairingCredentials(req.deviceId, req.authKey)
                    _selectedDigestUser.value = req.deviceId
                    _selectedDigestPass.value = req.authKey
                    _selectedPaired.value = true
                    _pairingError.value = null
                    _pairingRequest.value = null
                    _pairingError.value = null
                } else {
                    Log.d("TV", "Pairing failed")
                    _pairingError.value = "Pairing failed (invalid pin?)"
                }
            } catch (e: Exception) {
                _pairingError.value = e.message
            }
        }
    }

    fun markPairingComplete() {
        viewModelScope.launch {
            selectionStore.setSelectedPaired(true)
            _selectedPaired.value = true
        }
    }

    fun onRemoteAction(action: RemoteAction) {
        if (_demoMode.value) return
        viewModelScope.launch(Dispatchers.IO) {
            val ip = _selectedIp.value
            val basePath = _selectedBasePath.value
            if (ip.isNullOrBlank()) {
                Log.d("TV", "No selected TV IP for action $action")
                return@launch
            }

            val pin = trustStore.getPin(ip)
            if (pin.isNullOrBlank()) {
                Log.d("TV", "No stored pin for selected TV $ip")
                return@launch
            }

            val philipsKey = mapRemoteActionToPhilipsKey(action)
            if (philipsKey == null) {
                Log.d("TV", "No Philips key mapping for action $action")
                return@launch
            }

            try {
                val digestUser = _selectedDigestUser.value
                val digestPass = _selectedDigestPass.value
                if (digestUser.isNullOrBlank() || digestPass.isNullOrBlank()) {
                    Log.d("TV", "Missing digest credentials for selected TV $ip")
                    return@launch
                }

                remoteMutex.lock()
                try {
                    val success = remoteService.sendKey(ip, pin, basePath, digestUser, digestPass, philipsKey)
                    _tvOnline.value = success
                    Log.d("TV", "Remote action ... success=$success")
                } finally {
                    remoteMutex.unlock()
                }
            } catch (e: Exception) {
                _tvOnline.value = false
                Log.e("TV", "Remote action failed: $action", e)
            }
        }
    }

    fun updateCustomButton(index: Int, button: CustomButton) {
        viewModelScope.launch(Dispatchers.IO) {
            customButtonStore.set(index, button)
            val updated = _customButtons.value.toMutableList()
            updated[index] = button
            _customButtons.value = updated
        }
    }

    fun sendCustomCommand(command: String) {
        if (_demoMode.value) return
        viewModelScope.launch(Dispatchers.IO) {
            val ip = _selectedIp.value
            val basePath = _selectedBasePath.value
            if (ip.isNullOrBlank()) return@launch
            val pin = trustStore.getPin(ip)
            if (pin.isNullOrBlank()) return@launch
            val digestUser = _selectedDigestUser.value
            val digestPass = _selectedDigestPass.value
            if (digestUser.isNullOrBlank() || digestPass.isNullOrBlank()) return@launch
            try {
                remoteMutex.lock()
                try {
                    val intentJson = INTENT_COMMANDS[command]
                    val success = if (intentJson != null) {
                        remoteService.sendIntent(ip, pin, basePath, digestUser, digestPass, intentJson)
                    } else {
                        remoteService.sendKey(ip, pin, basePath, digestUser, digestPass, command)
                    }
                    _tvOnline.value = success
                } finally {
                    remoteMutex.unlock()
                }
            } catch (e: Exception) {
                _tvOnline.value = false
                Log.e("TV", "Custom command failed: $command", e)
            }
        }
    }

    private suspend fun subnetScan(): List<String> = withContext(Dispatchers.IO) {
        val localIps = try {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .filterNot { it.isLoopbackAddress }
                .mapNotNull { it.hostAddress }
        } catch (e: Exception) {
            Log.w("TV", "subnetScan: getNetworkInterfaces failed: ${e.message}")
            emptyList()
        }

        if (localIps.isEmpty()) return@withContext emptyList()

        val subnets = localIps.mapNotNull { ip ->
            val parts = ip.split(".")
            if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}." else null
        }.toSet()

        Log.d("TV", "subnetScan: subnets=$subnets")

        val semaphore = Semaphore(30)
        coroutineScope {
            subnets.flatMap { subnet ->
                (1..254).map { i ->
                    async {
                        semaphore.withPermit {
                            val ip = "$subnet$i"
                            if (isPortOpen(ip, 1926, 400)) ip else null
                        }
                    }
                }
            }.mapNotNull { it.await() }
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun mapRemoteActionToPhilipsKey(action: RemoteAction): String? {
        return when (action) {
            RemoteAction.VOLUME_UP -> "VolumeUp"
            RemoteAction.VOLUME_DOWN -> "VolumeDown"

            RemoteAction.CURSOR_UP -> "CursorUp"
            RemoteAction.CURSOR_DOWN -> "CursorDown"
            RemoteAction.CURSOR_LEFT -> "CursorLeft"
            RemoteAction.CURSOR_RIGHT -> "CursorRight"

            RemoteAction.OK -> "Confirm"
            RemoteAction.BACK -> "Back"

            RemoteAction.HOME -> "Home"
            RemoteAction.PLAY_PAUSE -> "PlayPause"
            RemoteAction.PAUSE -> "Pause"
            RemoteAction.STOP -> "Stop"

            RemoteAction.DIGIT_0 -> "Digit0"
            RemoteAction.DIGIT_1 -> "Digit1"
            RemoteAction.DIGIT_2 -> "Digit2"
            RemoteAction.DIGIT_3 -> "Digit3"
            RemoteAction.DIGIT_4 -> "Digit4"
            RemoteAction.DIGIT_5 -> "Digit5"
            RemoteAction.DIGIT_6 -> "Digit6"
            RemoteAction.DIGIT_7 -> "Digit7"
            RemoteAction.DIGIT_8 -> "Digit8"
            RemoteAction.DIGIT_9 -> "Digit9"

            RemoteAction.STANDBY -> "Standby"
            RemoteAction.OPTIONS -> "Options"
            RemoteAction.SOURCE -> "Source"
            RemoteAction.INFO -> "Info"
            RemoteAction.TV -> "WatchTV"
            RemoteAction.RED -> "RedColour"
            RemoteAction.GREEN -> "GreenColour"
            RemoteAction.YELLOW -> "YellowColour"
            RemoteAction.BLUE -> "BlueColour"

        }
    }

    companion object {
        private val INTENT_COMMANDS = mapOf(
            "NetflixIntent"          to """{"intent":{"action":"android.intent.action.MAIN","component":{"packageName":"com.netflix.ninja","className":"com.netflix.ninja.MainActivity"}}}""",
            "AmazonIntent"           to """{"intent":{"action":"android.intent.action.MAIN","component":{"packageName":"com.amazon.amazonvideo.livingroom","className":"com.amazon.ignition.IgnitionActivity"}}}""",
            "YoutubeIntent"          to """{"intent":{"action":"android.intent.action.MAIN","component":{"packageName":"com.google.android.youtube.tv","className":"com.google.android.youtube.tv.MainActivity"}}}""",
            "EpgIntent"              to """{"intent":{"action":"android.intent.action.MAIN","component":{"packageName":"org.droidtv.channels","className":"org.droidtv.channels.ChannelsActivity"}}}""",
            "LiveTvIntent"           to """{"intent":{"action":"android.intent.action.MAIN","component":{"packageName":"org.droidtv.channels","className":"org.droidtv.channels.ChannelsActivity"}}}""",
            "AndroidSettingsIntent"  to """{"intent":{"action":"android.settings.SETTINGS"}}""",
            "Hdmi1Intent"            to """{"intent":{"action":"org.droidtv.playtv.SELECTURI","extras":{"uri":"content://org.droidtv.playtv.provider.playtvdevicesprovider/devices/hdmi1"}}}""",
            "Hdmi2Intent"            to """{"intent":{"action":"org.droidtv.playtv.SELECTURI","extras":{"uri":"content://org.droidtv.playtv.provider.playtvdevicesprovider/devices/hdmi2"}}}""",
            "Hdmi3Intent"            to """{"intent":{"action":"org.droidtv.playtv.SELECTURI","extras":{"uri":"content://org.droidtv.playtv.provider.playtvdevicesprovider/devices/hdmi3"}}}""",
            "Hdmi4Intent"            to """{"intent":{"action":"org.droidtv.playtv.SELECTURI","extras":{"uri":"content://org.droidtv.playtv.provider.playtvdevicesprovider/devices/hdmi4"}}}"""
        )
    }

}
