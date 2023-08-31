package com.thenamlit.features.forex_factory_calendar.domain.use_case

import com.thenamlit.features.forex_factory_calendar.domain.model.*
import com.thenamlit.features.forex_factory_calendar.domain.repository.ForexFactoryCalendarRepository
import com.thenamlit.util.Resource
import io.ktor.http.*
import it.skrape.core.htmlDocument
import it.skrape.fetcher.Result
import it.skrape.selects.DocElement
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.html5.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class GetForexFactoryCalendarMonthUseCase(
    private val forexFactoryCalendarRepository: ForexFactoryCalendarRepository
) {
    suspend operator fun invoke(month: String, year: Int): Resource<ForexFactoryCalendarMonth> {
        return when (val getMonthRequestResult = forexFactoryCalendarRepository.getMonth(month = month, year = year)) {
            is Resource.Success -> {
                getMonthRequestResult.data?.let { scrapedMonthData: Result ->
                    getCalendarMonthFromScrapedData(scrapedDataResult = scrapedMonthData)
                } ?: run {
                    Resource.Error(
                        message = "Scraped Data from Forex Factory Calendar is undefined",
                        httpStatusCode = HttpStatusCode.InternalServerError
                    )
                }
            }

            is Resource.Error -> {
                println("GetForexFactoryCalendarMonthUseCase.Error -> ${getMonthRequestResult.httpStatusCode}")

                Resource.Error(
                    httpStatusCode = getMonthRequestResult.httpStatusCode,
                    message = getMonthRequestResult.message,
                    logging = getMonthRequestResult.logging
                )
            }
        }
    }

    private fun getCalendarMonthFromScrapedData(scrapedDataResult: Result): Resource<ForexFactoryCalendarMonth> {
        val calendarDays = mutableListOf<ForexFactoryCalendarDay>()

        return scrapedDataResult.htmlDocument {
            val calendarDayEntries = mutableListOf<ForexFactoryCalendarDayEntry>()
            var dateUnixTimestamp = 0L
            var previousDateTime = ForexFactoryCalendarDayEntryDateTime(
                timeStamp = dateUnixTimestamp,
                specialEvent = ForexFactoryCalendarDayEntryDateTimeSpecialEvent(
                    allDay = true,
                    isSpecialEvent = true,
                    description = ""
                ),
            )

            val calendarTableRows = table {
                withClass = "calendar__table"

                findFirst {
                    tbody {
                        findFirst {
                            tr {
                                withClass = "calendar__row"
                                findAll {
                                    this
                                }
                            }
                        }
                    }
                }
            }

            calendarTableRows.forEach { calendarTableRow: DocElement ->
//                println("CalendarTabRow:\n$calendarTableRow")

                if (calendarTableRow.hasClass("calendar__row--day-breaker")) {
//                    println("------------------")
//                    println("NEW DAY: ${calendarTableRow.td { findFirst { text } }}")

                    if (calendarDayEntries.isNotEmpty()) {
                        calendarDays.add(
                            ForexFactoryCalendarDay(
                                date = dateUnixTimestamp,
                                // Has to be converted to a list, otherwise it'll clear all previously
                                // inserted elements after the clear down below
                                entries = calendarDayEntries.toList()
                            )
                        )
                        calendarDayEntries.clear()
                    }
                } else {
                    if (calendarTableRow.hasAttribute("data-day-dateline")) {
                        dateUnixTimestamp = getCalendarRowDate(calendarTableRow = calendarTableRow)
//                        println("Data-Day-Dateline: $dateUnixTimestamp")
                    }

                    // http://www.unit-conversion.info/othertools/timestamp-to-date/
                    // Use the "GMT/UTC date and time"
                    val dateTime = getCalendarRowDateTime(
                        dateUnixTimestamp = dateUnixTimestamp,
                        previousValue = previousDateTime,
                        calendarTableRow = calendarTableRow
                    )
                    previousDateTime = dateTime

                    // Using UTC-Time for everything
                    val eventIsInTheFuture = dateTime.timeStamp >
                            LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)

                    val previousValue: ForexFactoryCalendarDayEntryPrevious =
                        getCalendarRowPrevious(calendarTableRow = calendarTableRow)

                    val forecastValue: String = getCalendarRowForecast(calendarTableRow = calendarTableRow)

                    calendarDayEntries.add(
                        ForexFactoryCalendarDayEntry(
                            eventTitle = getCalendarRowEventTitle(calendarTableRow = calendarTableRow),
                            currency = getCalendarRowCurrency(calendarTableRow = calendarTableRow),
                            impact = getCalendarRowImpact(calendarTableRow = calendarTableRow).name,
                            dateTime = dateTime,
                            actual = getCalendarRowActual(
                                calendarTableRow = calendarTableRow,
                                eventIsInTheFuture = eventIsInTheFuture,
                                forecastValue = forecastValue,
                                previousValue = previousValue.value
                            ),
                            forecast = forecastValue,
                            previous = previousValue,
                        )
                    )
                }
            }

            // Add the last day to the month
            calendarDays.add(
                ForexFactoryCalendarDay(
                    date = dateUnixTimestamp,
                    // Has to be converted to a list, otherwise it'll clear all previously inserted
                    // elements after the clear down below
                    entries = calendarDayEntries.toList()
                )
            )

            val calendarMonth = ForexFactoryCalendarMonth(days = calendarDays)
            Resource.Success(httpStatusCode = HttpStatusCode.OK, data = calendarMonth)
        }
    }

    private fun getCalendarRowEventTitle(calendarTableRow: DocElement): String {
        return try {
            calendarTableRow.td {
                withClass = "calendar__event"
                findFirst {
                    span {
                        findFirst {
                            text
                        }
                    }
                }
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowEventTitle - ElementNotFoundException - Returning empty String\n $e")
            ""
        }
    }

    private fun getCalendarRowCurrency(calendarTableRow: DocElement): String {
        return try {
            calendarTableRow.td {
                withClass = "calendar__currency"
                findFirst {
                    text
                }
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowCurrency - ElementNotFoundException - Returning empty String\n $e")
            ""
        }
    }

    private fun getCalendarRowImpact(calendarTableRow: DocElement): ForexFactoryCalendarDayEntryImpact {
        return try {
            val classNames: Set<String> = calendarTableRow.td {
                withClass = "calendar__impact"
                findFirst {
                    span {
                        findFirst {
                            classNames
                        }
                    }
                }
            }

            if (classNames.contains("icon--ff-impact-gra")) {
                ForexFactoryCalendarDayEntryImpact.NonEconomic
            } else if (classNames.contains("icon--ff-impact-yel")) {
                ForexFactoryCalendarDayEntryImpact.Low
            } else if (classNames.contains("icon--ff-impact-ora")) {
                ForexFactoryCalendarDayEntryImpact.Medium
            } else if (classNames.contains("icon--ff-impact-red")) {
                ForexFactoryCalendarDayEntryImpact.High
            } else {
                ForexFactoryCalendarDayEntryImpact.Unknown
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowImpact - ElementNotFoundException - Returning empty String\n $e")
            ForexFactoryCalendarDayEntryImpact.Unknown
        }
    }

    private fun getCalendarRowDate(calendarTableRow: DocElement): Long {
        return try {
            calendarTableRow.attribute(attributeKey = "data-day-dateline").toLong()
        } catch (e: NumberFormatException) {
//            println("getCalendarRowDate - NumberFormatException - Returning 0\n $e")
            0
        }
    }

    private fun getCalendarRowDateTime(
        dateUnixTimestamp: Long,
        previousValue: ForexFactoryCalendarDayEntryDateTime,
        calendarTableRow: DocElement
    ): ForexFactoryCalendarDayEntryDateTime {
        return try {
            val calendarTime: String = calendarTableRow.td {
                withClass = "calendar__time"
                findFirst {
                    text
                }
            }

            if (calendarTime.contains(other = "Day", ignoreCase = true) ||
                calendarTime.contains(other = "Tentative", ignoreCase = true)
            ) {
                return ForexFactoryCalendarDayEntryDateTime(
                    timeStamp = dateUnixTimestamp,
                    specialEvent = ForexFactoryCalendarDayEntryDateTimeSpecialEvent(
                        allDay = true,
                        isSpecialEvent = true,
                        description = calendarTime
                    ),
                )
            }

            when (calendarTime) {
                "" -> {
                    previousValue
                }

                else -> {
                    val localDateTimeBasedOnDateUnixTimeStamp = LocalDateTime.ofEpochSecond(
                        dateUnixTimestamp,
                        0,
                        ZoneOffset.UTC  // Using UTC-Time for everything
//                    OffsetDateTime.now().offset
                    )
//                val timeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d h:mma")   // am/pm
                    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:mm")    // 24hours
                    val fullLocalDateTime = LocalDateTime.parse(
                        "${localDateTimeBasedOnDateUnixTimeStamp.year}-" +
                                "${localDateTimeBasedOnDateUnixTimeStamp.monthValue}-" +
                                "${localDateTimeBasedOnDateUnixTimeStamp.dayOfMonth} " +
//                            calendarTime.uppercase(),   // am/pm
                                calendarTime,               // 24hours
                        timeFormatter
                    )

                    ForexFactoryCalendarDayEntryDateTime(
                        timeStamp = fullLocalDateTime.toEpochSecond(ZoneOffset.UTC),    // Using UTC-Time for everything
                        specialEvent = ForexFactoryCalendarDayEntryDateTimeSpecialEvent(
                            allDay = false,
                            isSpecialEvent = false,
                            description = calendarTime
                        ),
                    )
                }
            }
        } catch (e: Exception) {
//            println("getCalendarRowDateTime - Exception - Returning default CalendarDayEntryDateTime\n $e")
            ForexFactoryCalendarDayEntryDateTime(
                timeStamp = dateUnixTimestamp,
                specialEvent = ForexFactoryCalendarDayEntryDateTimeSpecialEvent(
                    allDay = true,
                    isSpecialEvent = true,
                    description = "Unknown"
                ),
            )
        }
    }

    private fun getCalendarRowActual(
        eventIsInTheFuture: Boolean,
        previousValue: String,
        forecastValue: String,
        calendarTableRow: DocElement
    ): ForexFactoryCalendarDayEntryActual {
        if (eventIsInTheFuture) {
            return ForexFactoryCalendarDayEntryActual(
                outcome = ForexFactoryCalendarDayEntryActualOutcome.FutureEvent.name,
                value = ""
            )
        }

        return try {
            val actualValue: DocElement = calendarTableRow.td {
                withClass = "calendar__actual"
                findFirst {
                    span {
                        findFirst {
                            this
                        }
                    }
                }
            }

            when (actualValue.className) {
                "" -> {
                    ForexFactoryCalendarDayEntryActual(
                        outcome = ForexFactoryCalendarDayEntryActualOutcome.AsExpected.name,
                        value = actualValue.text
                    )
                }

                "better" -> {
                    ForexFactoryCalendarDayEntryActual(
                        outcome = ForexFactoryCalendarDayEntryActualOutcome.Better.name,
                        value = actualValue.text
                    )
                }

                "worse" -> {
                    ForexFactoryCalendarDayEntryActual(
                        outcome = ForexFactoryCalendarDayEntryActualOutcome.Worse.name,
                        value = actualValue.text
                    )
                }

                else -> {
                    ForexFactoryCalendarDayEntryActual(
                        outcome = ForexFactoryCalendarDayEntryActualOutcome.Unknown.name,
                        value = actualValue.text
                    )
                }
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowActual - ElementNotFoundException - Returning empty String\n $e")

            if (previousValue == "" && forecastValue == "") {
                return ForexFactoryCalendarDayEntryActual(
                    outcome = ForexFactoryCalendarDayEntryActualOutcome.Empty.name,
                    value = ""
                )
            } else {
                ForexFactoryCalendarDayEntryActual(
                    outcome = ForexFactoryCalendarDayEntryActualOutcome.Unknown.name,
                    value = ""
                )
            }
        }
    }

    private fun getCalendarRowForecast(calendarTableRow: DocElement): String {
        return try {
            calendarTableRow.td {
                withClass = "calendar__forecast"
                findFirst {
                    span {
                        findFirst {
                            text
                        }
                    }
                }
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowForecast - ElementNotFoundException - Returning empty String\n $e")
            ""
        }
    }

    private fun getCalendarRowPrevious(calendarTableRow: DocElement): ForexFactoryCalendarDayEntryPrevious {
        return try {
            val actualValue: DocElement = calendarTableRow.td {
                withClass = "calendar__previous"
                findFirst {
                    span {
                        findFirst {
                            this
                        }
                    }
                }
            }

            when (actualValue.className) {
                "revised better" -> {
                    ForexFactoryCalendarDayEntryPrevious(
                        revision = ForexFactoryCalendarDayEntryPreviousRevision.RevisedBetter.name,
                        value = actualValue.text
                    )
                }

                "revised worse" -> {
                    ForexFactoryCalendarDayEntryPrevious(
                        revision = ForexFactoryCalendarDayEntryPreviousRevision.RevisedWorse.name,
                        value = actualValue.text
                    )
                }

                else -> {
                    ForexFactoryCalendarDayEntryPrevious(
                        revision = ForexFactoryCalendarDayEntryPreviousRevision.Normal.name,
                        value = actualValue.text
                    )
                }
            }
        } catch (e: ElementNotFoundException) {
//            println("getCalendarRowPrevious - ElementNotFoundException - Returning empty String\n $e")
            ForexFactoryCalendarDayEntryPrevious(
                revision = ForexFactoryCalendarDayEntryPreviousRevision.Unknown.name,
                value = ""
            )
        }
    }
}
