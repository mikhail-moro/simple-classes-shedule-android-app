package org.karasiksoftware.utils

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import java.time.LocalDate
import java.time.Period
import org.karasiksoftware.dataclasses.calendar.CalendarItem
import org.karasiksoftware.dataclasses.meeting.Meeting


class CalendarUtils {
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
    fun getMonthSchedule(data: Map<String, List<Meeting>>, date: LocalDate): List<CalendarItem> {
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
                    days.add(CalendarItem(true, day, data[reformatDate(day)]!!))
                } catch (e: java.lang.NullPointerException) {
                    days.add(CalendarItem(true, day, listOf()))
                }
            } else {
                try {
                    days.add(CalendarItem(false, day, data[reformatDate(day)]!!))
                } catch (e: java.lang.NullPointerException) {
                    days.add(CalendarItem(false, day, listOf()))
                }
            }
        }

        return days
    }

    // Returns CalendarItem`s object with data from first arg
    fun getDaySchedule(data: Map<String,List<Meeting>>, date: LocalDate): CalendarItem {
        return try {
            CalendarItem(true, date, data[reformatDate(date)]!!)
        } catch (e: java.lang.NullPointerException) {
            CalendarItem(true, date, listOf())
        }
    }
}