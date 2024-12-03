package com.cs407.virtualwiscard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LogoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logout)

        val myWebView: WebView = findViewById(R.id.webView)

        //Enable javascript
        myWebView.settings.javaScriptEnabled = true

        //UW login SSO
        myWebView.loadUrl("https://my.wisc.edu/portal/Logout")

        //Handle the page redirection and page loading
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false //Allow the webView load the URL
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //Check if success
                Log.d("LogoutActivity", "URL: $url")
                if (url != null) {
                    //If Success move to login page
                    Log.d("LogoutActivity", "moving to login page...")
                    val intent = Intent(this@LogoutActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() //Close so cant return
                }
            }
        }
    }
}