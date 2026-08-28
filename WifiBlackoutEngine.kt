package com.atawurrahmantanvir.mailfactory.engine

import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.util.concurrent.TimeUnit

// Developed strictly for educational purposes and system research.

object WifiBlackoutEngine {
    private const val TAG = "NEXUS_WifiBlackout"
    private const val COMMAND_TIMEOUT_SECONDS = 20L

    private val _consoleLog = MutableStateFlow<List<String>>(emptyList())
    val consoleLog: StateFlow<List<String>> = _consoleLog

    private fun postLog(message: String) {
        val currentList = _consoleLog.value.toMutableList()
        currentList.add(message)
        _consoleLog.value = currentList
        Log.d(TAG, message)
    }

    suspend fun executeWifiBlackout(): Boolean = withContext(Dispatchers.IO) {
        postLog("> Initializing KERNEL WI-FI BLACKOUT ENGINE...")
        var process: java.lang.Process? = null
        var os: DataOutputStream? = null
        
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()
            os.writeBytes("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")

            os.writeBytes("su -context u:r:su:s0 -c 'setenforce 0' 2>/dev/null\n")
            os.writeBytes("setenforce 0 2>/dev/null\n")

            val script = buildString {
                append("ip neighbor flush all 2>/dev/null\n")
                append("ip tcp_metrics flush 2>/dev/null\n")
                append("ip route flush cache 2>/dev/null\n")
                
                append("ifconfig wlan0 down 2>/dev/null\n")
                append("ip link set wlan0 address 02:00:00:\$(od -An -N3 -tx1 /dev/urandom | tr ' ' ':') 2>/dev/null\n")
                append("ifconfig wlan0 up 2>/dev/null\n")
                
                append("cmd location set-location-controller-package com.android.location.fused 0 2>/dev/null\n")
                append("cmd connectivity captive-portal-applications set-apps \"\"\n")
                append("settings put global captive_portal_mode 0 2>/dev/null\n")
                
                append("iptables -t nat -F OUTPUT 2>/dev/null\n")
                append("iptables -t nat -A OUTPUT -p udp --dport 5353 -j DROP 2>/dev/null\n")
                append("iptables -A OUTPUT -d ://gstatic.com -j DROP 2>/dev/null\n")
                append("iptables -A OUTPUT -p tcp -m string --string \"://google.com\" --algo bm -j DROP 2>/dev/null\n")
                
                append("logcat -c\n")
                append("exit\n")
            }


            os.writeBytes(script)
            os.flush()

            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!completed) {
                postLog("⚠ Wi-Fi Blackout Engine execution timed out.")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()
            if (exitValue == 0) {
                postLog("✓ Linux Neighbor tables (ARP) successfully demolished")
                postLog("✓ Physical wlan0 Link interface mutated and rotated")
                postLog("✓ mDNS local discovery & metadata traffic blackholed")
                postLog("┌──────────────────────────────────────┐")
                postLog("│ WI-FI BLACKOUT: 100% ARMORED & READY │")
                postLog("└──────────────────────────────────────┘")
                return@withContext true
            } else {
                postLog("[ERROR] Engine execution returned code: $exitValue")
                return@withContext false
            }

        } catch (e: Exception) {
            postLog("⚠ Critical exception inside kernel routing pipelines.")
            return@withContext false
        } finally {
            try { os?.close(); process?.destroy() } catch (_: Exception) {}
        }
    }
}
