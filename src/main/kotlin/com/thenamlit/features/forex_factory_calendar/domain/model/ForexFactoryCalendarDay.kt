package com.thenamlit.features.forex_factory_calendar.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class ForexFactoryCalendarDay(
    val date: Long,
    val entries: List<ForexFactoryCalendarDayEntry> = emptyList()
)
