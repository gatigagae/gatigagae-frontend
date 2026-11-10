package com.gatigagae.app.data

import java.util.Locale

/** 화면 문구를 만드는 작은 도구들 */
object TextUtils {

    /** 받침이 있는 한글 글자인지 확인. 예: '밤' → true, '이' → false */
    private fun hasBatchim(c: Char): Boolean {
        if (c < '가' || c > '힣') return false
        return (c.code - '가'.code) % 28 != 0
    }

    /** "콩이" → "콩이랑", "밤" → "밤이랑", 이름 없음 → "반려견이랑" (S1-03) */
    fun withRang(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "반려견이랑"
        return if (hasBatchim(trimmed.last())) trimmed + "이랑" else trimmed + "랑"
    }

    /** "콩이" → "콩이가", "밤" → "밤이", 이름 없음 → "반려견이" */
    fun withIga(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "반려견이"
        return if (hasBatchim(trimmed.last())) trimmed + "이" else trimmed + "가"
    }

    /** 800 → "800m", 1400 → "1.4km" */
    fun distance(meters: Int): String =
        if (meters < 1000) "${meters}m" else String.format(Locale.KOREA, "%.1fkm", meters / 1000.0)

    /** 84 → "1시간 24분", 30 → "30분", 120 → "2시간" */
    fun drivingTime(minutes: Int): String {
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours == 0 -> "${rest}분"
            rest == 0 -> "${hours}시간"
            else -> "${hours}시간 ${rest}분"
        }
    }

    /** 26.5 → "26.5℃" */
    fun temperature(value: Double): String = String.format(Locale.KOREA, "%.1f℃", value)
}
