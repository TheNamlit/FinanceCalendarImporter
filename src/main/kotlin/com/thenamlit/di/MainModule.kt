package com.thenamlit.di

import com.thenamlit.features.forex_factory_calendar.data.repository.ForexFactoryCalendarRepositoryImpl
import com.thenamlit.features.forex_factory_calendar.domain.repository.ForexFactoryCalendarRepository
import com.thenamlit.features.forex_factory_calendar.domain.use_case.GetForexFactoryCalendarMonthUseCase
import kotlinx.serialization.json.Json
import org.koin.dsl.module


val mainModule = module {
    single<ForexFactoryCalendarRepository> {
        ForexFactoryCalendarRepositoryImpl()
    }

    single<GetForexFactoryCalendarMonthUseCase> {
        GetForexFactoryCalendarMonthUseCase(forexFactoryCalendarRepository = get())
    }

    single<Json> {
        Json { ignoreUnknownKeys = true }
    }
}
