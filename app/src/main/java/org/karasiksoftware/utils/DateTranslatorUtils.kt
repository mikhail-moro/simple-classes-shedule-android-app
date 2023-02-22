package org.karasiksoftware.utils

// Class that contains methods to translate date to russian
class DateTranslatorUtils {
    fun getRusMonth(number: Int): String {
        return when (number) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            else -> "декабря"
        }
    }

    fun getRusDayOfWeek(day: Int): String {
        return when (day) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            else -> "Воскресенье"
        }
    }
}