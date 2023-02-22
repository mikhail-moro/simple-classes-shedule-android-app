package org.karasiksoftware.dataclasses.database

data class DatabaseMeetingsData
(
    val meetingsSize: Int,
    val meetingsIndex: Int,
    val meetingsNames: List<String>,
    val meetingsStarts: List<String>,
    val meetingsEnds: List<String>,
    val meetingsAuds: List<String>
)