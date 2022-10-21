package com.example.lessonsschedule

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import java.time.LocalDate

data class CalendarItem(
    val status: Boolean, // Determines whether day contains in current month
    val date: LocalDate,
    val meetings: List<Meeting> // List of data of meetings for day
)
