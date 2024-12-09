package com.cs407.virtualwiscard

import android.content.ContentValues.TAG
import android.content.Context
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class MyHostApduService : HostApduService() {
    companion object {
        private const val SELECT_APDU_HEADER = "00A40400" // APDU command for SELECT
        private const val RESPONSE_OK = "9000" // Success response
        private const val RESPONSE_ERROR = "6F00" // Error response
    }

    override fun processCommandApdu(apdu: ByteArray, extras: Bundle): ByteArray {
        Log.d("HCE", "Received APDU: ${bytesToHex(apdu)}")
        val apduHex = bytesToHex(apdu)

        return when {
            apduHex.startsWith(SELECT_APDU_HEADER) -> {

                val sharedPreferences = getSharedPreferences("appPrefs", Context.MODE_PRIVATE)
                val wiscardNumber = sharedPreferences.getString("wiscardNumberForNFC", "Unknown Wiscard Number")

                val responseBytes = wiscardNumber!!.toByteArray(Charsets.UTF_8)
                Log.d(TAG, "Sending Wiscard Number: $wiscardNumber")
                buildResponse(responseBytes)
            }
            else -> hexStringToByteArray(RESPONSE_ERROR)
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "onDeactivated called with reason: $reason")
        when (reason) {
            DEACTIVATION_LINK_LOSS -> Log.d("HCE", "Deactivation due to link loss.")
            DEACTIVATION_DESELECTED -> Log.d("HCE", "Deactivation due to deselection.")
            else -> Log.d("HCE", "Deactivation for unknown reason.")
        }
    }

    private fun buildResponse(data: ByteArray): ByteArray {
        return concatenateArrays(data, hexStringToByteArray(RESPONSE_OK))
    }

    private fun concatenateArrays(array1: ByteArray, array2: ByteArray): ByteArray {
        return array1 + array2 // Kotlin's concise array concatenation
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        require(s.length % 2 == 0) { "Hex string must have an even length." }
        return ByteArray(s.length / 2) {
            s.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
}