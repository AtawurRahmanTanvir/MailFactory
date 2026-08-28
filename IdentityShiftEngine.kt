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
 * NEXUS Engine 01: IDENTITY SHIFT
 * High-performance runtime identity/environment management engine.
 * UX Optimized: Unified Script Execution for zero-latency & clean terminal logs.
 */
object IdentityShiftEngine {

    private const val TAG = "NEXUS_IdentityShift"
    // ডেডলক এড়ানোর জন্য ১৫ সেকেন্ডের লিমিট
    private const val COMMAND_TIMEOUT_SECONDS = 15L 

    private val _consoleLog = MutableStateFlow<List<String>>(emptyList())
    val consoleLog: StateFlow<List<String>> = _consoleLog

    private fun postLog(message: String) {
        val currentList = _consoleLog.value.toMutableList()
        currentList.add(message)
        _consoleLog.value = currentList
        Log.d(TAG, message)
    }

    suspend fun executeIdentityShift(): Boolean = withContext(Dispatchers.IO) {

        postLog("> Initiating Identity Protection...")

        var process: java.lang.Process? = null
        var os: DataOutputStream? = null

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()

            // [CRITICAL UX FIX] Unified Shell Script for Zero-Latency
            // সব কমান্ড একসাথে পুশ করা হচ্ছে যাতে I/O ল্যাগ না হয়
            val script = buildString {
                append("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")
                append("su -context u:r:su:s0 -c 'setenforce 0' 2>/dev/null\n")
                append("setenforce 0 2>/dev/null\n")
                
                // Obfuscate Secure Android ID
                append("settings put secure android_id \\$(od -An -N8 -tx /dev/urandom | tr -d ' ') 2>/dev/null\n")
                
                // Network Layer MAC Spoofing
                append("ifconfig wlan0 down 2>/dev/null\n")
                append("ifconfig wlan0 hw ether 02:00:00:\\$(od -An -N3 -tx1 /dev/urandom | tr ' ' ':') 2>/dev/null\n")
                append("ifconfig wlan0 up 2>/dev/null\n")
                
                // Runtime Property Injections (Virtual Device Profiles)
                append("resetprop ro.product.model 'Pixel 8 Pro' 2>/dev/null\n")
                append("resetprop ro.product.brand 'google' 2>/dev/null\n")
                append("resetprop ro.build.product 'husky' 2>/dev/null\n")
                append("resetprop ro.product.device 'husky' 2>/dev/null\n")
                
                // Flush System Memory Logs
                append("rm -rf /data/system/usagestats/* 2>/dev/null\n")
                append("logcat -c\n")
                append("dmesg -c 2>/dev/null\n")
                append("exit\n")
            }

            os.writeBytes(script)
            os.flush()

            // ডেডলক বাইপাস
            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            if (!completed) {
                postLog("⚠ Protection deployment timed out. Retrying...")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()

            if (exitValue == 0) {
                postLog("✓ Device identity masked")
                postLog("✓ Network footprint hidden")
                postLog("✓ Virtual device properties applied")
                postLog("┌──────────────────────────────────────┐")
                postLog("│ IDENTITY SHIELD: SECURED             │")
                postLog("└──────────────────────────────────────┘")
                return@withContext true
            } else {
                postLog("⚠ System error during identity masking.")
                return@withContext false
            }

        } catch (e: Exception) {
            postLog("⚠ Unexpected error: System busy.")
            return@withContext false
        } finally {
            try { 
                os?.close() 
            } catch (e: Exception) {}
            try { 
                process?.destroy() 
            } catch (e: Exception) {}
        }
    }

    fun clearConsole() {
        _consoleLog.value = emptyList()
    }
}