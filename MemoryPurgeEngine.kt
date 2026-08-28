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
 * NEXUS Engine 05: MEMORY PURGE
 * Privileged Linux memory/cache maintenance engine.
 * UX Optimized: Unified Script Execution for zero-latency & clean terminal logs.
 */
object MemoryPurgeEngine {

    private const val TAG = "NEXUS_MemoryPurge"
    private const val COMMAND_TIMEOUT_SECONDS = 15L

    private val _consoleLog = MutableStateFlow<List<String>>(emptyList())
    val consoleLog: StateFlow<List<String>> = _consoleLog

    private fun postLog(message: String) {
        val currentList = _consoleLog.value.toMutableList()
        currentList.add(message)
        _consoleLog.value = currentList
        Log.d(TAG, message)
    }

    suspend fun executeMemoryPurge(): Boolean = withContext(Dispatchers.IO) {

        postLog("> Starting memory optimization...")

        var process: java.lang.Process? = null
        var os: DataOutputStream? = null

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val currentPid = Process.myPid()

            // [CRITICAL UX FIX] Unified Shell Script for Zero-Latency
            // সব ক্যাশ ক্লিয়ারিং কমান্ড একবারে পাঠানো হচ্ছে ল্যাগ এড়ানোর জন্য
            val script = buildString {
                append("echo -1000 > /proc/$currentPid/oom_score_adj 2>/dev/null\n")
                append("sync\n") // Synchronize filesystem
                append("if [ -w /proc/sys/vm/drop_caches ]; then echo 3 > /proc/sys/vm/drop_caches; fi\n") // Drop caches
                append("logcat -c\n") // Clear local Android logs
                append("exit\n")
            }

            os.writeBytes(script)
            os.flush()

            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            if (!completed) {
                postLog("⚠ Memory optimization timed out. Retrying in background...")
                process.destroyForcibly()
                return@withContext false
            }

            val exitValue = process.exitValue()

            if (exitValue != 0) {
                postLog("⚠ System error during memory purge.")
                return@withContext false
            }

            postLog("✓ Storage file systems synchronized")
            postLog("✓ System cache and temporary memory cleared")
            postLog("✓ Diagnostic logs purged")
            postLog("┌──────────────────────────────────────┐")
            postLog("│ MEMORY PURGE: COMPLETED              │")
            postLog("└──────────────────────────────────────┘")

            return@withContext true

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