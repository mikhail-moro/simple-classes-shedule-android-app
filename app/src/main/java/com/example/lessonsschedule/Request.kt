package com.example.lessonsschedule

import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate


class Request {
    // Sends GET-request and execute code from changeTable arg with data from response
    @OptIn(DelicateCoroutinesApi::class)
    fun getData(startDate: LocalDate, endDate: LocalDate, changeTable:(data: String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://mikhailmoro.pythonanywhere.com/?start_time=${reformatDate(startDate)}T00:00:00&end_time=${reformatDate(endDate)}T23:59:59")

                val inputStream: InputStream
                val conn = url.openConnection() as HttpURLConnection

                conn.connect()
                inputStream = conn.inputStream

                changeTable(inputStream.bufferedReader().use { it.readText() })
            } catch (e: FileNotFoundException) {
                changeTable("{}")
            }
        }
    }

    // Returns string with date in ISO 8601 format
    private fun reformatDate(date: LocalDate): String {
        var reformattedDate = date.year.toString()

        if (date.monthValue <= 9) {
            reformattedDate += "-0${date.monthValue}"
        } else {
            reformattedDate += "-${date.monthValue}"
        }

        if (date.dayOfMonth <= 9) {
            reformattedDate += "-0${date.dayOfMonth}"
        } else {
            reformattedDate += "-${date.dayOfMonth}"
        }

        return reformattedDate
    }
}