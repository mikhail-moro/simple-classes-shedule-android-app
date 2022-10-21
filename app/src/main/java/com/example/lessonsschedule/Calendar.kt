package com.example.lessonsschedule

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import kotlinx.serialization.decodeFromString
import java.time.LocalDate
import java.time.Period
import kotlinx.serialization.json.Json


class Calendar {
    // Decode response-data from arg to Map<String,List<Meeting>> object
    private fun getMeetings(data: String): Map<String,List<Meeting>> {
        return Json.decodeFromString<Map<String,List<Meeting>>>(data)
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

    // Returns list of CalendarItem`s objects with data from first arg to month table cells
    fun getMonthSchedule(data: String, date: LocalDate): List<CalendarItem> {
        val meetings = getMeetings(data)
        val firstDayOfMonth = LocalDate.of(date.year, date.month, 1)
        val diff: Int = firstDayOfMonth.dayOfWeek.value - 1
        val days: MutableList<CalendarItem> = mutableListOf<CalendarItem>()

        for (i in (-diff)..(41-diff)) {
            val day = firstDayOfMonth.plus(Period.of(0, 0, i))

            if (day.dayOfWeek.toString() == "SUNDAY") {
                continue
            }

            if (day.month == date.month) {
                try {
                    days.add(CalendarItem(true, day, meetings[reformatDate(day)]!!))
                } catch (e: java.lang.NullPointerException) {
                    days.add(CalendarItem(true, day, listOf()))
                }
            } else {
                try {
                    days.add(CalendarItem(false, day, meetings[reformatDate(day)]!!))
                } catch (e: java.lang.NullPointerException) {
                    days.add(CalendarItem(false, day, listOf()))
                }
            }
        }

        return days
    }

    // Returns CalendarItem`s object with data from first arg
    fun getDaySchedule(data: String, date: LocalDate): CalendarItem {
        val meetings = getMeetings(data)

        return try {
            CalendarItem(true, date, meetings[reformatDate(date)]!!)
        } catch (e: java.lang.NullPointerException) {
            CalendarItem(true, date, listOf())
        }
    }
}