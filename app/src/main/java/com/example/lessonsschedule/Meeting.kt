package com.example.lessonsschedule

import kotlinx.serialization.Serializable

@Serializable
data class Meeting(
    val module: String? = null,
    val name: String? = null,
    val theme: String? = null,
    val type: String? = null,
    val aud: String? = null,
    val link: String? = null,
    val teachers: String? = null,
    val groups: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val color: String? = null
)