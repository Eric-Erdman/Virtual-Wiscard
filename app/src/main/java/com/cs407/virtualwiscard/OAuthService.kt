import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object OAuthService {

    fun getAccessToken(clientId: String, clientSecret: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.wisc.edu/oauth/token")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val credentials = "$clientId:$clientSecret"
                val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                connection.setRequestProperty("Authorization", authHeader)

                connection.doOutput = true
                val body = "grant_type=client_credentials"
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(body.toByteArray())
                outputStream.flush()
                outputStream.close()

                //handle response
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val accessToken = parseAccessToken(response)
                    callback(accessToken)
                } else {
                    Log.e("OAuthService", "Error: Response Code $responseCode")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("OAuthService", "Error fetching access token", e)
                callback(null)
            }
        }
    }

    private fun parseAccessToken(jsonResponse: String): String? {
        return try {
            val jsonObject = org.json.JSONObject(jsonResponse)
            jsonObject.getString("access_token")
        } catch (e: Exception) {
            Log.e("OAuthService", "Error parsing access token", e)
            null
        }
    }
}
