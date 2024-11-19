package com.cs407.virtualwiscard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum
import com.nightonke.boommenu.BoomButtons.HamButton
import com.nightonke.boommenu.BoomMenuButton
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore




class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val clientId = "input key here"
        val clientSecret = "input secret here"

        // Step 1: Get Access Token
        OAuthService.getAccessToken(clientId, clientSecret) { accessToken ->
            if (accessToken != null) {
                // Step 2: Use Access Token to Call Mock Person API
                val mockPersonApiUrl = "https://mock.api.wisc.edu/people"
                ApiService.fetchPeople(mockPersonApiUrl, accessToken) { people ->
                    runOnUiThread {
                        people.forEach { person ->
                            Log.d("MainActivity", "Person: $person")
                        }
                    }
                }
            } else {
                Log.e("MainActivity", "Failed to fetch access token")
            }
        }



        val bmb = findViewById<BoomMenuButton>(R.id.bmb)

        bmb.setButtonEnum(ButtonEnum.Ham)
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_3)
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3)

        bmb.clearBuilders()

        for (i in 0 until bmb.piecePlaceEnum.pieceNumber()) {
            val builder = HamButton.Builder()
                .normalImageRes(R.drawable.uw_logo)
                .normalText("Item ${i + 1}")
                .normalColor(Color.RED)
                .listener { index ->
                    Toast.makeText(this, "Clicked item $index", Toast.LENGTH_SHORT).show()
                }

            bmb.addBuilder(builder)
        }

        // Initialize NFC Adapter
        val nfcErrorMessage = findViewById<TextView>(R.id.nfcErrorMessage)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            //if NFC not supported show message
            nfcErrorMessage.visibility = View.VISIBLE
        } else {
            //Prepare NFC Foreground Dispatch
            pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        }
    }


    override fun onResume() {
        super.onResume()
        //Allow foreground dispatch to handle NFC tags
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        //Disable foreground dispatch when the activity is paused
        nfcAdapter?.disableForegroundDispatch(this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        //superclass call
        super.onNewIntent(intent)

        //handle tag discovery for NFC
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            tag?.let {
                handleNfcTag(it)
            }
        }
    }

    private fun handleNfcTag(tag: Tag) {

        //This is where we will take the received tag and
        //Appropriately attempt to unlock the door given which building it is

        //simulate unlocking door
        unlockDoor()

        //confirm
        Toast.makeText(this, "Door unlocked successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun unlockDoor() {
        //logic we will add to communicate with server and send correct frequency based on needs
        //to unlock necessary door based on access levels etc and send message
        println("Door unlocked")
    }
}