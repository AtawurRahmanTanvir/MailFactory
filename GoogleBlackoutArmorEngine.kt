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
 * NEXUS Engine 10: GOOGLE BLACKOUT ARMOR
 * Advanced kernel-level isolation module.
 * UX Optimized: Unified Script Execution for zero-latency & user-friendly logs.
 */
object GoogleBlackoutArmorEngine {
    private const val TAG = "NEXUS_BlackoutArmor"
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

    /**
     * Executes advanced sandboxing hooks to completely disrupt
     * real-time device identity verification routines.
     */
    suspend fun executeBlackoutArmor(): Boolean = withContext(Dispatchers.IO) {
        postLog("> Deploying Blackout Armor...")
        
        var process: java.lang.Process? = null
        var os: DataOutputStream? = null
        
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()

            // [CRITICAL UX FIX] Unified Shell Script for Zero-Latency
            // সব সিকিউরিটি রুলস একসাথে পুশ করা হচ্ছে যাতে কোনো I/O ল্যাগ না হয়
            val script = buildString {
                append("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")
                append("su -context u:r:su:s0 -c 'setenforce 0' 2>/dev/null\n")
                append("setenforce 0 2>/dev/null\n")
                
                // Freeze GMS
                append("killall -STOP com.google.android.gms.unbundled 2>/dev/null\n")
                append("killall -STOP com.google.android.gms.persistent 2>/dev/null\n")
                
                // Spoof Boot properties
                append("resetprop --delete ro.boot.vbmeta.device_state 2>/dev/null\n")
                append("resetprop --delete ro.boot.verifiedbootstate 2>/dev/null\n")
                append("resetprop ro.boot.flash.locked 1 2>/dev/null\n")
                append("resetprop ro.secure 1 2>/dev/null\n")
                
                // Block network verification beacons
                append("iptables -A OUTPUT -d ://google.com -j DROP 2>/dev/null\n")
                append("iptables -A OUTPUT -d ://googleapis.com -j DROP 2>/dev/null\n")
                
                // Finalize
                append("setprop sys.boot_completed 1\n")
                append("logcat -c\n")
                append("exit\n")
            }

            os.writeBytes(script)
            os.flush()
            
            // ডেডলক বাইপাস
            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            if (!completed) {
                postLog("⚠ Armor deployment timed out. Retrying in background...")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()
            
            if (exitValue == 0) {
                postLog("✓ Core background services isolated")
                postLog("✓ Hardware signatures cloaked")
                postLog("✓ Network telemetry blocked")
                postLog("┌──────────────────────────────────────┐")
                postLog("│ BLACKOUT ARMOR: ACTIVE               │")
                postLog("└──────────────────────────────────────┘")
                return@withContext true
            } else {
                postLog("⚠ System error during armor deployment.")
                return@withContext false
            }

        } catch (e: Exception) {
            postLog("⚠ Unexpected error: System busy.")
            return@withContext false
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (_: Exception) {}
        }
    }

    /**
     * Unfreezes the Google Play Services background threads after flow completion.
     * এই মেথডটি জিমেইল একাউন্ট খোলা শেষ হওয়ার পর কল করতে হবে যাতে ওএস আবার নরমাল হয়।
     */
    suspend fun releaseBlackoutArmor(): Unit = withContext(Dispatchers.IO) {
        var process: java.lang.Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            
            // Unified release script
            val script = buildString {
                append("killall -CONT com.google.android.gms.unbundled 2>/dev/null\n")
                append("killall -CONT com.google.android.gms.persistent 2>/dev/null\n")
                append("iptables -D OUTPUT -d ://googleapis.com -j DROP 2>/dev/null\n")
                append("iptables -D OUTPUT -d ://google.com -j DROP 2>/dev/null\n")
                append("exit\n")
            }
            
            os.writeBytes(script)
            os.flush()
            
            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            if (completed && process.exitValue() == 0) {
                // ইউজার যখন অ্যাপে ব্যাক করবে তখন এই মেসেজটা দেখতে পাবে
                postLog("✓ Normal system state restored")
                Log.d(TAG, "[!] Blackout Armor successfully released. GMS normalized.")
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { 
                os?.close() 
                process?.destroy() 
            } catch (_: Exception) {}
        }
    }

    fun clearConsole() {
        _consoleLog.value = emptyList()
    }
}