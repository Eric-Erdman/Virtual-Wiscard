package com.cs407.virtualwiscard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import android.webkit.CookieManager
import okhttp3.*
import org.json.JSONException

import org.json.JSONObject
import java.io.IOException



class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)


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
//            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//                val url = request?.url.toString()
//                Log.d("LoginActivity", "Intercepted request: $url")
//                return super.shouldInterceptRequest(view, request)
//            }

            fun fetchAndLogUsername() {
                val cookies = CookieManager.getInstance().getCookie("https://my.wisc.edu")
                if (cookies == null) {
                    Log.e("LoginActivity", "No cookies found. Cannot fetch user details.")
                    return
                }

                Log.d("LoginActivity", "Cookies: $cookies") // Log cookies for debugging

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://my.wisc.edu/portal/web/session.json") // Update with your endpoint
                    .addHeader("Cookie", cookies) // Pass the cookies to authenticate the request
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("LoginActivity", "Failed to fetch session details: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            try {
                                val json = JSONObject(responseData ?: "{}")
                                val person = json.getJSONObject("person")
                                val userName = person.getString("userName")
                                val firstName = person.getString("firstName")
                                val lastName = person.getString("lastName")

                                Log.d("LoginActivity", "Fetched Username: $userName")
                                Log.d("LoginActivity", "Fetched First Name: $firstName")
                                Log.d("LoginActivity", "Fetched Last Name: $lastName")

                                // Save all details to SharedPreferences
                                saveUserDetailsToPreferences(userName, firstName, lastName)

                            } catch (e: Exception) {
                                Log.e("LoginActivity", "Error parsing JSON: ${e.message}")
                            }
                        } else {
                            Log.e("LoginActivity", "Error fetching session: ${response.code}")
                        }
                    }
                })
            }

            fun fetchAndLogWiscardNumber() {
                val cookies = CookieManager.getInstance().getCookie("https://my.wisc.edu")
                if (cookies == null) {
                    Log.e("LoginActivity", "No cookies found")
                    return
                }

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://my.wisc.edu/myuw-bell-notification-backend/") // Confirm this endpoint
                    .addHeader("Cookie", cookies)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("LoginActivity", "Failed to fetch Wiscard number: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            try {
                                val jsonObject = JSONObject(responseData)
                                val headersReceived = jsonObject.getJSONObject("headers-received")
                                val wiscardNumber = headersReceived.optString("wisceduwiscardaccountnumber", "Not Available")

                                Log.d("LoginActivity", "Wiscard Number Retrieved: $wiscardNumber")

                                // Save the Wiscard Number
                                val sharedPreferences = getSharedPreferences("appPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("wiscardNumber", wiscardNumber).apply()
                                Log.d("LoginActivity", "Wiscard Number Saved: $wiscardNumber")

                                // Move to MainActivity only after saving
                                runOnUiThread {
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish() // Close LoginActivity
                                }
                            } catch (e: JSONException) {
                                Log.e("LoginActivity", "Failed to parse JSON: ${e.message}")
                            }
                        } else {
                            Log.e("LoginActivity", "Failed to fetch Wiscard number: ${response.code}")
                        }
                    }



                })
            }



            fun saveUserDetailsToPreferences(username: String, firstName: String, lastName: String) {
                val sharedPref = getSharedPreferences("VirtualWiscardPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("username", username)
                editor.putString("firstName", firstName)
                editor.putString("lastName", lastName)
                editor.apply()

                Log.d("LoginActivity", "User details saved: $username, $firstName, $lastName")
            }











            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //Check if success
                val cookieManager = android.webkit.CookieManager.getInstance()
                val cookies = cookieManager.getCookie("https://my.wisc.edu/web/expanded")
                Log.d("LoginActivity", "Cookies for my.wisc.edu: $cookies")
                myWebView.evaluateJavascript("(function() { return document.body.innerHTML; })();") { html ->
                    Log.d("LoginActivity", "Page HTML: $html")
                }
                fetchAndLogUsername() //should get the users info and log it
                fetchAndLogWiscardNumber()


                Log.d("LoginActivity", "URL: $url")
                if (url != null && url == "https://my.wisc.edu/web/expanded") {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() //Close so cant return
                }
            }
        }
    }
}