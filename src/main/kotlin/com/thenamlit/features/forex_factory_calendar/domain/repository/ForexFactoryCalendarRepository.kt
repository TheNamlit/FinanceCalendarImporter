package com.thenamlit.features.forex_factory_calendar.domain.repository

import com.thenamlit.util.Resource
import it.skrape.fetcher.Result


interface ForexFactoryCalendarRepository {
    suspend fun getMonth(month: String, year: Int): Resource<Result>
}
