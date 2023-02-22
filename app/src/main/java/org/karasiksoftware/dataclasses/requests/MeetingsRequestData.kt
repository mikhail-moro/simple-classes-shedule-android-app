package org.karasiksoftware.dataclasses.requests

import org.karasiksoftware.dataclasses.meeting.Meeting

@kotlinx.serialization.Serializable
data class MeetingsData(
    private val raspList: List<Meeting?>? = null
) {
    private fun fillMap(): Map<String, List<Meeting>> {
        val map: MutableMap<String, MutableList<Meeting>> = mutableMapOf()

        for (i in raspList!!) {
            val dateString = i!!.start!!.slice(0..9)

            if (map.containsKey(dateString)) {
                map[dateString]!!.add(i)
            } else {
                map[dateString] = mutableListOf(i)
            }
        }

        return map
    }

    val meetingsMap: Map<String, List<Meeting>> = fillMap()
}

@kotlinx.serialization.Serializable
data class MeetingsRequestData(
    private val data: MeetingsData? = null,
    val state: Int? = null
) {
    val meetings = data!!.meetingsMap
}