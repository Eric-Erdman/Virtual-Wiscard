package com.cs407.virtualwiscard

import android.content.ContentValues.TAG
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class NFCReadingActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val logList = mutableListOf<String>()
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_reading)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val backButton: Button = findViewById(R.id.backButton)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        logAdapter = LogAdapter(logList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = logAdapter


        enableReaderMode()

        backButton.setOnClickListener {
            finish()
        }
    }

    //back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(
            this,
            { tag ->
                Log.d(TAG, "NFC Tag Detected")
                handleTag(tag)
            },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    private fun handleTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                val command = hexStringToByteArray("00A4040007F0010203040506")
                val response = isoDep.transceive(command)

                //convert to readable string
                val responseStr = String(response)
                Log.d(TAG, "Received APDU Response: $responseStr")

                //update with string
                runOnUiThread {
                    logList.add(responseStr)
                    logAdapter.notifyItemInserted(logList.size - 1)
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
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

}