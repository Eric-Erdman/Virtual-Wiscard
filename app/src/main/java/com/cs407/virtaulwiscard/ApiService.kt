import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

object ApiService {

    fun fetchPeople(apiUrl: String, accessToken: String, callback: (List<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val people = parsePeople(response)
                    callback(people)
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("ApiService", "Error: Response Code $responseCode\nError Message: $errorStream")
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Error fetching people", e)
            }
        }
    }

    private fun parsePeople(jsonResponse: String): List<String> {
        val people = mutableListOf<String>()
        try {
            val jsonObject = org.json.JSONObject(jsonResponse)
            val dataArray = jsonObject.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val person = dataArray.getJSONObject(i)
                val attributes = person.getJSONObject("attributes")
                val name = "${attributes.getString("firstName")} ${attributes.getString("lastName")}"
                people.add(name)
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error parsing people data", e)
        }
        return people
    }
}
