# Finance Calendar Importer

## An importer of calendar data, coming from [ForexFactory](https://www.forexfactory.com).

# Features & State

### Implemented

- Fetch data from the [ForexFactory-Calendar](https://www.forexfactory.com/calendar).
- Use website scraping to get actual data and serialize it.

### Missing / Next up

- Create connection to Database.
- Download fetched/scraped data to Database.
- CryptoCraft-Calendar.
- (Consider Metals & Energy Calendar too).
- Authentication.
- Optimize Docker-Implementation.
- Deploy?

## Libraries

- [Koin (Ktor)](https://insert-koin.io/docs/reference/koin-ktor/ktor) for dependency injection.
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON serialization.
- [scrape.it](https://github.com/skrapeit/skrape.it) for website scraping.

## Additional Info

This project is still under active development and will be updated whenever I find time for it.  
It is mainly used for demonstration purposes and for me to learn new technologies like website scraping.

I plan on connecting this to a database (probably MongoDB), so that the calendar data is easily accessible. I would like
to then use this data to eventually create personal calendar events in something like my Google Calendar.

## License

[MIT](https://choosealicense.com/licenses/mit/)
