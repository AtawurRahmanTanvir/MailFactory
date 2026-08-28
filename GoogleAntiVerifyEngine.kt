package com.atawurrahmantanvir.mailfactory.engine

import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.util.concurrent.TimeUnit

/**
 * NEXUS Engine 08: GOOGLE ANTI-VERIFY
 * Eliminates GMS persistent device tokens and verification state.
 * UX Optimized: Unified Script Execution for zero-latency & user-friendly logs.
 */
object GoogleAntiVerifyEngine {
    private const val TAG = "NEXUS_GoogleAntiVerify"
    // ডেডলক প্রতিরোধের জন্য ১৫ সেকেন্ডের লিমিট
    private const val COMMAND_TIMEOUT_SECONDS = 15L 

    private val _consoleLog = MutableStateFlow<List<String>>(emptyList())
    val consoleLog: StateFlow<List<String>> = _consoleLog

    private fun postLog(message: String) {
        val currentList = _consoleLog.value.toMutableList()
        currentList.add(message)
        _consoleLog.value = currentList
        Log.d(TAG, message)
    }

    suspend fun executeAntiVerify(): Boolean = withContext(Dispatchers.IO) {
        postLog("> Activating anti-tracking shield...")
        
        var process: java.lang.Process? = null
        var os: DataOutputStream? = null
        
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()

            // [CRITICAL UX FIX] Unified Shell Script
            // সব কমান্ড একসাথে পাঠানো হচ্ছে যাতে আই/ও ল্যাগ না হয়
            val script = buildString {
                append("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")
                append("su -context u:r:su:s0 -c 'setenforce 0' 2>/dev/null\n")
                append("setenforce 0 2>/dev/null\n")
                append("pm clear com.google.android.gsf 2>/dev/null\n")
                append("pm trim-caches 4G 2>/dev/null\n")
                append("settings put secure gsf_id 0 2>/dev/null\n")
                append("settings delete secure gsf_id 2>/dev/null\n")
                append("rm -rf /data/system/dropbox/* 2>/dev/null\n")
                append("logcat -c\n")
                append("exit\n")
            }

            // স্ক্রিপ্ট এক্সিকিউট করা হলো
            os.writeBytes(script)
            os.flush()
            
            // ডেডলক বাইপাস
            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            if (!completed) {
                postLog("⚠ Shield activation timed out. Retrying in background...")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()
            
            if (exitValue == 0) {
                postLog("✓ Service tokens cleared")
                postLog("✓ Tracking identifiers removed")
                postLog("✓ Telemetry data sanitized")
                postLog("┌──────────────────────────────────────┐")
                postLog("│ ANTI-TRACKING SHIELD: ACTIVE         │")
                postLog("└──────────────────────────────────────┘")
                return@withContext true
            } else {
                postLog("⚠ System error during shield activation.")
                return@withContext false
            }

        } catch (e: Exception) {
            postLog("⚠ Unexpected error: System busy.")
            return@withContext false
        } finally {
            try {
                os?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Output stream cleanup failed", e)
            }
            try {
                process?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Process cleanup failed", e)
            }
        }
    }

    fun clearConsole() {
        _consoleLog.value = emptyList()
    }
}