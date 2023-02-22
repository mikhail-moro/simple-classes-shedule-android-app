package org.karasiksoftware.utils

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.karasiksoftware.dataclasses.meeting.Meeting
import org.karasiksoftware.dataclasses.requests.MeetingsRequestData
import org.karasiksoftware.dataclasses.requests.TokenRequestData
import java.nio.charset.Charset


class RequestUtils {
    companion object {
        private const val GET_DATA_URL = "https://edu.donstu.ru/api/RaspManager"
        private const val GET_TOKEN_URL = "https://edu.donstu.ru/api/tokenauth"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun getStudyYears(month: Int, year: Int): String {
        return if (month in 1..9) {
            "${year-1}-$year"
        } else {
            "$year-${year+1}"
        }
    }

    fun getData(month: Int, year: Int, token: String): Map<String, List<Meeting>> {

        try {
            val response = Fuel.get("$GET_DATA_URL?educationSpaceID=4&month=$month&showJournalFilled=false&year=${getStudyYears(month, year)}")
                .header(Headers.CONTENT_TYPE, "application/json; charset=utf-8")
                .authentication()
                .bearer(token)
                .responseString()

            val data = json.decodeFromString<MeetingsRequestData>(response.third.get())
            return data.meetings
        } catch (ex: Exception) {
            ex.printStackTrace()

            return if (ex.message == "Connection reset" || ex.message == "timeout") {
                Log.w("karasiki", "Catch")
                getData(month, year, token)
            } else {
                emptyMap()
            }
        } catch (er: Error) {
            er.printStackTrace()
            return emptyMap()
        }
    }

    fun getToken(username: String, password: String): TokenRequestData {

        try {
            val body = JSONObject()
            body.put("userName", username)
            body.put("password", password)

            val response = Fuel.post(GET_TOKEN_URL)
                .header(Headers.CONTENT_TYPE, "application/json; charset=utf-8")
                .jsonBody(body.toString(), Charset.defaultCharset())
                .responseString()

            return json.decodeFromString(response.third.get())
        } catch (ex: Exception) {
            ex.printStackTrace()

            return if (ex.message == "Connection reset" || ex.message == "timeout") {
                Log.w("karasiki", "Catch")
                getToken(username, password)
            } else {
                TokenRequestData()
            }
        } catch (er: Error) {
            er.printStackTrace()
            return TokenRequestData()
        }
    }
}
