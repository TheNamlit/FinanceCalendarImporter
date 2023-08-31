package com.thenamlit.features.forex_factory_calendar.data.repository

import com.thenamlit.features.forex_factory_calendar.domain.repository.ForexFactoryCalendarRepository
import com.thenamlit.util.Resource
import io.ktor.http.*
import it.skrape.fetcher.*


class ForexFactoryCalendarRepositoryImpl : ForexFactoryCalendarRepository {
    override suspend fun getMonth(month: String, year: Int): Resource<Result> {

        // https://docs.skrape.it/docs/dsl/extracting-data-from-websites
        // https://www.forexfactory.com/calendar?month=may.2007
        // https://docs.skrape.it/docs/parser/parsing-html
        // https://github.com/skrapeit/skrape.it
        return try {
            val scrapedData = skrape(BrowserFetcher) {
                request {
                    url = "https://www.forexfactory.com/calendar?month=$month.$year"
                    method = Method.GET
                    cookies = mapOf(
//                        "fftimezoneoffset" to "12",
//                        "fftimezone" to "Pacific%2FAuckland",
                        "fftimezoneoffset" to "0",      // Using UTC-Time for everything
                        "fftimezone" to "Etc%2FUTC",    // Using UTC-Time for everything
                        "fftimeformat" to "1",
                    )
                }

                response {
                    this
                }
            }

            return when (scrapedData.responseStatus.code) {
                200 -> {
                    Resource.Success(
                        httpStatusCode = HttpStatusCode.OK,
                        data = scrapedData,
                        logging = scrapedData.responseStatus.message
                    )
                }

                else -> {
                    Resource.Error(
                        httpStatusCode = HttpStatusCode(
                            value = scrapedData.responseStatus.code,
                            description = scrapedData.responseStatus.message
                        ),
                        message = "Couldn't get Data from ForexFactory (${scrapedData.responseStatus.code})",
                        logging = "Couldn't get Data from ForexFactory (${scrapedData.responseStatus.code})",
                    )
                }
            }
        } catch (e: Exception) {
            println("Exception: $e")

            Resource.Error(
                httpStatusCode = HttpStatusCode.InternalServerError,
                message = "Failed to Request or Fetch Data from ForexFactory",
                logging = e.message ?: "Failed to Request or Fetch Data from ForexFactory"
            )
        }
    }
}
