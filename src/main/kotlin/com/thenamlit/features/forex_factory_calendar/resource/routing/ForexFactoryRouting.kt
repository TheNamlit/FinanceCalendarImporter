package com.thenamlit.features.forex_factory_calendar.resource.routing

import com.thenamlit.features.forex_factory_calendar.domain.model.ForexFactoryCalendarMonth
import com.thenamlit.features.forex_factory_calendar.domain.use_case.GetForexFactoryCalendarMonthUseCase
import com.thenamlit.util.Resource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.ktor.ext.inject


fun Route.forexFactoryRouting() {
    val getForexFactoryCalendarMonthUseCase: GetForexFactoryCalendarMonthUseCase by inject()

    get<ForexFactoryCalendar> { forexFactoryCalendar: ForexFactoryCalendar ->
        val forexFactoryCalendarMonthRequest: Deferred<Resource<ForexFactoryCalendarMonth>> =
            async(Dispatchers.IO) {
                getForexFactoryCalendarMonthUseCase(
                    month = forexFactoryCalendar.month,
                    year = forexFactoryCalendar.year
                )
            }

        when (val calendarMonthResult = forexFactoryCalendarMonthRequest.await()) {
            is Resource.Success -> {
                calendarMonthResult.data?.let { forexFactoryCalendarMonth: ForexFactoryCalendarMonth ->
                    // TODO: Save in Database instead of returning the data - just return a message
                    //  saying "Data is being imported"
                    //  Should probably create a status for the importing states to avoid multiple imports
                    //  - Import requested, Import in progress, Imported

                    call.respond(
                        status = calendarMonthResult.httpStatusCode ?: HttpStatusCode.OK,
                        message = forexFactoryCalendarMonth
                    )
                } ?: run {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = "Data from Forex Factory Calendar is undefined"
                    )
                }
            }

            is Resource.Error -> {
                call.respond(
                    status = calendarMonthResult.httpStatusCode ?: HttpStatusCode.InternalServerError,
                    message = calendarMonthResult.message ?: "Failed to fetch Forex Factory Calendar Data"
                )
            }
        }
    }
}
