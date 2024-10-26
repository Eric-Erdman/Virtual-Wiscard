package com.cs407.virtaulwiscard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val myWebView: WebView = findViewById(R.id.webView)

        //Enable javascript
        myWebView.settings.javaScriptEnabled = true

        //UW login SSO
        myWebView.loadUrl("https://my.wisc.edu/web/expanded")

        //Handle the page redirection and page loading
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false //Allow the webView load the URL
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //Check if success
                if (url?.contains("success_page_indicator") == true) {
                    //If Success move to main page
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() //Close so cant return
                }
            }
        }
    }
}