package com.atawurrahmantanvir.mailfactory.engine

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NEXUS Engine: GOOGLE ACCOUNT
 * Launches Android's native Google account-add flow securely.
 * UX Optimized: Added terminal logging and standard package name.
 */
class GoogleAccountEngine(
    private val activity: Activity
) {
    private val TAG = "NEXUS_GoogleAccount"

    private val _consoleLog = MutableStateFlow<List<String>>(emptyList())
    val consoleLog: StateFlow<List<String>> = _consoleLog

    private fun postLog(message: String) {
        val currentList = _consoleLog.value.toMutableList()
        currentList.add(message)
        _consoleLog.value = currentList
        Log.d(TAG, message)
    }

    /**
     * MAIN ENTRY POINT
     * Launches the native Android account flow safely.
     */
    fun openSafely(): Boolean {
        return try {
            postLog("> Initializing secure account gateway...")

            val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
            
            // Specifically request the Google account authenticator
            intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            
            // Ensures a clean launch without keeping previous stuck states
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Launch the native Android account flow
            activity.startActivity(intent)

            postLog("✓ Secure account environment launched")
            true

        } catch (exception: Exception) {
            postLog("⚠ Failed to launch account gateway.")
            exception.printStackTrace()
            false
        }
    }

    /**
     * Resets the console buffer state.
     */
    fun clearConsole() {
        _consoleLog.value = emptyList()
    }
}