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
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val universityBanner: ImageView = findViewById(R.id.universityBanner)
        universityBanner.setOnLongClickListener {
            //go to nfc reader page
            val intent = Intent(this, ReaderActivity::class.java)
            startActivity(intent)
            true
        }

        val clientId = "input key here"
        val clientSecret = "input secret here"

        val sharedPref = getSharedPreferences("VirtualWiscardPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("username", "Guest")
        val firstName = sharedPref.getString("firstName", "Unknown")
        val lastName = sharedPref.getString("lastName", "User")

        val sharedPreferences = getSharedPreferences("appPrefs", Context.MODE_PRIVATE)
        val wiscardNumber = sharedPreferences.getString("wiscardNumber", "Not Available")
        Log.d("MainActivity", "Retrieved Wiscard Number: $wiscardNumber")

        val testButton: Button = findViewById(R.id.testButton)
        testButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("VirtualWiscardPrefs", MODE_PRIVATE)
            val username = sharedPreferences.getString("username", "hu434") ?: "hu434"

            MyHostApduService.updateUserAccessCache(this, username) { success ->
                if (success) {
                    Log.d("MainActivity", "Access cache update succeeded for user: $username")
                    Toast.makeText(this, "Cache updated successfully for $username", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MainActivity", "Access cache update failed for user: $username")
                    Toast.makeText(this, "Failed to update cache for $username", Toast.LENGTH_SHORT).show()
                }
            }
        }

        sharedPreferences.edit().putString("wiscardNumberForNFC", wiscardNumber).apply()

        //get access token
        OAuthService.getAccessToken(clientId, clientSecret) { accessToken ->
            if (accessToken != null) {
                //call mock person using token
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
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_2)
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_2)

        bmb.clearBuilders()

        for (i in 0 until bmb.piecePlaceEnum.pieceNumber()) {
            val builder = HamButton.Builder()
                .normalImageRes(R.drawable.uw_logo)
                .normalText(if (i == 0) "Wiscard Balance" else "Logout")
                .textSize(23)
                .textPadding(Rect(0,6,0,0))
                .normalColor(Color.RED)
                .highlightedColor(Color.WHITE)
                .listener { index ->
                    if (index == 0) {
                        val intent = Intent(this@MainActivity, BalanceActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MainActivity, LogoutActivity::class.java)
                        startActivity(intent)
                    }
                }

            bmb.addBuilder(builder)
        }

        val nfcErrorMessage = findViewById<TextView>(R.id.nfcErrorMessage)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            nfcErrorMessage.visibility = View.VISIBLE
        } else {
            pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        }

        //set the name and stuff
        val nameTextView: TextView = findViewById(R.id.studentName)
        val fullName = "$firstName $lastName"
        nameTextView.text = fullName
        val wiscardNumberTextView: TextView = findViewById(R.id.studentID)
        wiscardNumberTextView.text = wiscardNumber
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
        Toast.makeText(this, "Door unlocked successfully!", Toast.LENGTH_SHORT).show()
    }
}