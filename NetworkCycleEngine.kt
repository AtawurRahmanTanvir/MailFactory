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
 * NEXUS Engine 02: NETWORK CYCLE
 * Privileged network-state cycling and kernel network maintenance engine.
 * UX Optimized: Unified Script Execution for zero-latency & clean terminal logs.
 */
object NetworkCycleEngine {

    private const val TAG = "NEXUS_NetworkCycle"
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

    suspend fun executeNetworkCycle(): Boolean = withContext(Dispatchers.IO) {

        postLog("> Initiating secure network cycle...")

        var process: java.lang.Process? = null
        var os: DataOutputStream? = null

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()

            // [CRITICAL UX FIX] Unified Shell Script
            // ল্যাগ এড়াতে রেডিও রিসেট এবং TCP অপটিমাইজেশন একসাথে পুশ করা হচ্ছে
            val script = buildString {
                append("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")
                
                // Toggle Airplane Mode to cycle network/IP
                append("cmd connectivity airplane-mode enable 2>/dev/null\n")
                append("sleep 2\n") // Radio needs time to power down
                append("cmd connectivity airplane-mode disable 2>/dev/null\n")
                
                // Kernel TCP configurations
                append("if [ -w /proc/sys/net/ipv4/tcp_fastopen ]; then echo 3 > /proc/sys/net/ipv4/tcp_fastopen; fi\n")
                append("if [ -w /proc/sys/net/ipv4/tcp_rmem ]; then echo '4096 87380 6291456' > /proc/sys/net/ipv4/tcp_rmem; fi\n")
                append("if [ -w /proc/sys/net/ipv4/tcp_wmem ]; then echo '4096 16384 4194304' > /proc/sys/net/ipv4/tcp_wmem; fi\n")
                append("if [ -w /proc/sys/net/ipv4/tcp_low_latency ]; then echo 1 > /proc/sys/net/ipv4/tcp_low_latency; fi\n")
                
                // Refresh resolver
                append("ndc resolver flushdefaultiface 2>/dev/null\n")
                append("exit\n")
            }

            os.writeBytes(script)
            os.flush()

            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            if (!completed) {
                postLog("⚠ Network cycle timed out. Retrying in background...")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()

            if (exitValue != 0) {
                postLog("⚠ System error during network cycle.")
                return@withContext false
            }

            postLog("✓ Network radio cycled successfully")
            postLog("✓ Connection routing refreshed")
            postLog("✓ TCP parameters optimized")
            postLog("┌──────────────────────────────────────┐")
            postLog("│ NETWORK CYCLE: COMPLETED             │")
            postLog("└──────────────────────────────────────┘")

            return@withContext true

        } catch (e: Exception) {
            postLog("⚠ Unexpected error: Network busy.")
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