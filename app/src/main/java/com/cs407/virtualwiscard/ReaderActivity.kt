package com.cs407.virtualwiscard

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class ReaderActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var logAdapter: LogAdapter? = null
    private var logList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        logList = ArrayList()
        logAdapter = LogAdapter(logList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = logAdapter

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Back button logic
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed() // Navigates to the previous screen
        }

        enableReaderMode()
    }

    private fun enableReaderMode() {
        nfcAdapter!!.enableReaderMode(this, { tag: Tag ->
            Log.d(TAG, "NFC Tag Detected")
            handleTag(tag)
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    private fun handleTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                val command =
                    hexStringToByteArray("00A4040007F0010203040506") // Example APDU command
                val response = isoDep.transceive(command)

                // Convert the response to a readable string
                val responseStr = String(response) // Converts bytes to a string
                Log.d(
                    TAG,
                    "Received APDU Response: $responseStr"
                )

                // Update the RecyclerView with the string representation
                runOnUiThread {
                    logList.add(responseStr)
                    logAdapter!!.notifyItemInserted(logList.size - 1)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error communicating with NFC tag", e)
            } finally {
                try {
                    isoDep.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing IsoDep", e)
                }
            }
        }
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

    private fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02X", b))
        }
        return result.toString()
    }

    companion object {
        private const val TAG = "NFCReceiver"
    }
}