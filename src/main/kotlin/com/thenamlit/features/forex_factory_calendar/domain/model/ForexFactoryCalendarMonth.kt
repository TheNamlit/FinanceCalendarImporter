package com.thenamlit.features.forex_factory_calendar.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class ForexFactoryCalendarMonth(
    val days: List<ForexFactoryCalendarDay> = emptyList()
)
