package com.example.capp

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class MyHostApduService : HostApduService() {
    override fun processCommandApdu(apdu: ByteArray, extras: Bundle): ByteArray {
        Log.d("HCE", "Received APDU: " + bytesToHex(apdu))
        val apduHex = bytesToHex(apdu)

        if (apduHex.startsWith(SELECT_APDU_HEADER)) {
            // Handle the SELECT command
            val message = "Hello NFC!"
            val responseBytes = message.toByteArray()
            Log.d("HCE", "APDU response: " + bytesToHex(responseBytes))
            return buildResponse(message.toByteArray())
        }

        // Return error if command is not recognized
        return hexStringToByteArray(RESPONSE_ERROR)
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
        val result = ByteArray(array1.size + array2.size)
        System.arraycopy(array1, 0, result, 0, array1.size)
        System.arraycopy(array2, 0, result, array1.size, array2.size)
        return result
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02X", b))
        }
        return result.toString()
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
            i += 2
        }
        return data
    }

    companion object {
        private const val SELECT_APDU_HEADER = "00A40400" // APDU command for SELECT
        private const val RESPONSE_OK = "9000" // Success response
        private const val RESPONSE_ERROR = "6F00" // Error response

        private const val KEEP_ALIVE_APDU = "00" // Empty APDU for keeping connection alive
    }
}