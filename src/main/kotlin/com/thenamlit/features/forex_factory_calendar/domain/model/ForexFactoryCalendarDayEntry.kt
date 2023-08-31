package com.thenamlit.features.forex_factory_calendar.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class ForexFactoryCalendarDayEntry(
    val eventTitle: String,
    val currency: String,
    val impact: String,
    val dateTime: ForexFactoryCalendarDayEntryDateTime,
    val actual: ForexFactoryCalendarDayEntryActual,
    val forecast: String,
    val previous: ForexFactoryCalendarDayEntryPrevious,
)

@Serializable
data class ForexFactoryCalendarDayEntryDateTime(
    val timeStamp: Long,
    val specialEvent: ForexFactoryCalendarDayEntryDateTimeSpecialEvent,
)

@Serializable
data class ForexFactoryCalendarDayEntryDateTimeSpecialEvent(
    val isSpecialEvent: Boolean,
    val allDay: Boolean,
    val description: String
)

@Serializable
sealed class ForexFactoryCalendarDayEntryImpact(val name: String) {
    @Serializable
    data object NonEconomic : ForexFactoryCalendarDayEntryImpact(name = "NonEconomic")

    @Serializable
    data object Low : ForexFactoryCalendarDayEntryImpact(name = "Low")

    @Serializable
    data object Medium : ForexFactoryCalendarDayEntryImpact(name = "Medium")

    @Serializable
    data object High : ForexFactoryCalendarDayEntryImpact(name = "High")

    @Serializable
    data object Unknown : ForexFactoryCalendarDayEntryImpact(name = "Unknown")
}


@Serializable
data class ForexFactoryCalendarDayEntryActual(
    val outcome: String,
    val value: String
)

@Serializable
sealed class ForexFactoryCalendarDayEntryActualOutcome(val name: String) {
    @Serializable
    data object AsExpected : ForexFactoryCalendarDayEntryActualOutcome(name = "AsExpected")

    @Serializable
    data object Better : ForexFactoryCalendarDayEntryActualOutcome(name = "Better")

    @Serializable
    data object Worse : ForexFactoryCalendarDayEntryActualOutcome(name = "Worse")

    @Serializable
    data object FutureEvent : ForexFactoryCalendarDayEntryActualOutcome(name = "FutureEvent")

    @Serializable
    data object Empty : ForexFactoryCalendarDayEntryActualOutcome(name = "Empty")

    @Serializable
    data object Unknown : ForexFactoryCalendarDayEntryActualOutcome(name = "Unknown")
}

@Serializable
data class ForexFactoryCalendarDayEntryPrevious(
    val revision: String,
    val value: String
)

@Serializable
sealed class ForexFactoryCalendarDayEntryPreviousRevision(val name: String) {
    @Serializable
    data object Normal : ForexFactoryCalendarDayEntryPreviousRevision(name = "Normal")

    @Serializable
    data object RevisedBetter : ForexFactoryCalendarDayEntryPreviousRevision(name = "RevisedBetter")

    @Serializable
    data object RevisedWorse : ForexFactoryCalendarDayEntryPreviousRevision(name = "RevisedWorse")

    @Serializable
    data object Unknown : ForexFactoryCalendarDayEntryPreviousRevision(name = "Unknown")
}
