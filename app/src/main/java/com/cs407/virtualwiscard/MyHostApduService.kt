package com.cs407.virtualwiscard

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import android.content.Context

class MyHostApduService : HostApduService() {

    companion object {
        private const val TAG = "MyHostApduService"
        // Define the application's AID
        private const val AID = "F0010203040506"
        // Construct the SELECT AID command
        private val SELECT_APDU = buildSelectApdu(AID)

        private fun buildSelectApdu(aid: String): ByteArray {
            return hexStringToByteArray(
                "00A40400" + String.format("%02X", aid.length / 2) + aid
            )
        }

        private fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(TAG, "Received APDU: ${commandApdu?.joinToString(" ") { String.format("%02X", it) }}")

        if (commandApdu == null) {
            return ByteArray(0)
        }

        // Check if the command is a SELECT AID command
        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.d(TAG, "AID selected")

            // Check user permissions
            val userHasAccess = checkUserAccess()

            val responseData = if (userHasAccess) {
                "PASS" // User has permission
            } else {
                "FAIL" // User does not have permission
            }

            return responseData.toByteArray(Charsets.UTF_8) + byteArrayOf(
                0x90.toByte(),
                0x00.toByte()
            ) // Success status word
        } else {
            // Non-SELECT commands return an error status word
            return byteArrayOf(0x6F.toByte(), 0x00.toByte())
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    // Check if the user has permission
    private fun checkUserAccess(): Boolean {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("has_access", false)
    }
}
