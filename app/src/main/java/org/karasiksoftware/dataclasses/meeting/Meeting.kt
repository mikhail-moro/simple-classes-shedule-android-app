package org.karasiksoftware.dataclasses.meeting

@kotlinx.serialization.Serializable
data class Meeting(
    val name: String? = null,
    val color: String? = null,
    val start: String? = null,
    val end: String? = null,
    private val info: MeetingInfo? = null
) {
    private val inf = info!!
    val module = inf.moduleName
    val theme = inf.theme
    val aud = inf.aud
    val teachers = inf.teachersNames
    val group = inf.groupName
    val type = inf.type
    val link = inf.link
    val startTime = start!!.slice(11..15)
    val endTime = end!!.slice(11..15)

    val isOnline = link != "" && link?.contains(".") ?: false
}

@kotlinx.serialization.Serializable
data class MeetingInfo(
    val moduleName: String? = null,
    val theme: String? = null,
    val aud: String? = null,
    val link: String? = null,
    val teachersNames: String? = null,
    val groupName: String? = null,
    val type: String? = null
)