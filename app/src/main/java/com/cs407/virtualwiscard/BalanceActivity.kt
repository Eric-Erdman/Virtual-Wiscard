package com.cs407.virtualwiscard

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum
import com.nightonke.boommenu.BoomButtons.HamButton
import com.nightonke.boommenu.BoomMenuButton
import com.nightonke.boommenu.ButtonEnum
import com.nightonke.boommenu.Piece.PiecePlaceEnum

class BalanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_balance)

        val webView: WebView = findViewById(R.id.webview_balance)
        webView.settings.apply {
            javaScriptEnabled = true // Enable JavaScript
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Cache settings
            loadWithOverviewMode = true // Load page to fit WebView screen
            useWideViewPort = true // Enable viewport for responsive design
            builtInZoomControls = true // Allow zooming
            displayZoomControls = false // Disable zoom controls overlay
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Use JavaScript to scroll to the desired section
                webView.evaluateJavascript(
                    """
                    (function() {
                        //get balance table
                        var table = document.getElementById('Wiscard Account');
                        
                        if (table) {
                            document.body.innerHTML = '';
                            //add only table
                            document.body.appendChild(table);
                    
                            //all white background
                            var rows = table.getElementsByTagName('tr');
                            for (var i = 0; i < rows.length; i++) {
                                rows[i].style.backgroundColor = 'white'; // Set background to white
                            }
                        }
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
        webView.loadUrl("https://online.wiscard.wisc.edu/login.php?cid=120")

        val bmb = findViewById<BoomMenuButton>(R.id.bmb)

        bmb.setButtonEnum(ButtonEnum.Ham)
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_2)
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_2)

        bmb.clearBuilders()

        for (i in 0 until bmb.piecePlaceEnum.pieceNumber()) {
            val builder = HamButton.Builder()
                .normalImageRes(R.drawable.uw_logo)
                .normalText(if (i == 0) "Homepage" else "Logout")
                .textSize(23)
                .textPadding(Rect(0,6,0,0))
                .normalColor(Color.RED)
                .highlightedColor(Color.WHITE)
                .listener { index ->
                    if (index == 0) {
                        val intent = Intent(this@BalanceActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else { // Define behavior for the logout button
                        val intent = Intent(this@BalanceActivity, LogoutActivity::class.java)
                        startActivity(intent)
                    }
                }

            bmb.addBuilder(builder)
        }
    }
}