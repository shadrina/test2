package hellodb

import java.math.BigDecimal
import java.util.*
import javax.persistence.*


/**
 * Классы ORM модели.
 * Планеты
 */
@Entity(name = "planet")
class PlanetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
    var name: String? = null
    var distance: BigDecimal? = null
    @OneToMany(mappedBy = "planet")
    var flights: MutableList<FlightEntity>? = null
}

/**
 * Классы ORM модели.
 * Полеты
 */
@Entity(name = "flightentityview")
class FlightEntity {
    @Id
    var id: Int? = null
    var date: Date? = null
    var available_seats: Int? = null
    @ManyToOne
    @JoinColumn(name = "planet_id")
    var planet: PlanetEntity? = null
}

/**
 * Основной код обработчика HTTP запросов.
 */
class FlightsHandler {
    private val flightCache = mutableMapOf<Int, FlightEntity>()

    /**
     * Отображает таблицу с полетами в указанную дату или со всеми полетами,
     * если дата не указана
     *
     * Пример: /flights?flight_date=2084-06-12
     *         /flights
     */
    fun handleFlights(flightDate: Date?): String {
        // Let's fetch the data we need. Hopefully it is cached
        val tablebody = cacheFlights(flightDate)
                // Convert entity ids to entities
                .map { flightCache[it] }
                .filterNotNull()
                // and convert every entity to a table row
                .map {
                    """
|<tr>
|   <td>${it.id}</td>
|   <td>${it.date}</td>
|   <td>${it.planet?.name}</td>
|   <td>${it.planet?.id}</td>
|</tr>""".trimMargin()
                }.joinToString(separator = "\n")
        return "$FLIGHTS_HEADER $tablebody $FLIGHTS_FOOTER"
    }

    fun handleFlightsCorrected(flightDate: Date?): String {
        val result = StringBuilder()
        withConnection(true) { connection ->
            if (flightDate == null) {
                connection.prepareStatement("""SELECT F.id, F.date, P.name AS planet_name, P.id AS planet_id 
                                                  FROM Flight F 
                                                  JOIN Planet P ON F.planet_id = P.id""")
            } else {
                connection.prepareStatement("""SELECT F.id, F.date, P.name AS planet_name, P.id AS planet_id 
                                                  FROM Flight F 
                                                  JOIN Planet P ON F.planet_id = P.id 
                                                  WHERE F.date=?""").also { statement ->
                    statement.setDate(1, java.sql.Date(flightDate.time))
                }
            }.use { prepared ->
                prepared.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        result.append("""
|<tr>
|   <td>${resultSet.getInt("id")}</td>
|   <td>${resultSet.getDate("date")}</td>
|   <td>${resultSet.getString("planet_name")}</td>
|   <td>${resultSet.getInt("planet_id")}</td>
|</tr>
            """.trimMargin()).append("\n")
                    }
                }
            }
        }
        return "$FLIGHTS_HEADER $result $FLIGHTS_FOOTER"
    }

    /**
     * Сдвигает полёты, начинающиеся в указанную дату на указанный интервал.
     * Формат даты: yyyy-MM-dd (например 2019-12-19)
     * Формат интервала: 1day, 2weeks, и так далее.
     * https://www.postgresql.org/docs/current/datatype-datetime.html#DATATYPE-INTERVAL-INPUT
     *
     * пример: /delay_flights?flight_date=2084-06-12&interval=1day
     *
     * Возвращает строку с количеством обновленных полетов
     */
    fun handleDelayFlights(flightDate: Date, interval: String): String {
        var updateCount = 0
        cacheFlights(flightDate).forEach { flightId ->
            withConnection(true) {
                updateCount += it.prepareStatement("UPDATE Flight SET date=date + interval '$interval' WHERE id=$flightId")
                        .executeUpdate()
            }
        }
        return "Updated $updateCount flights"
    }

    fun handleDelayFlightsCorrected(flightDate: Date, interval: String): String {
        var updateCount = 0
        withConnection(true) {
            val statement = it.prepareStatement("UPDATE Flight SET date=date + interval '$interval' WHERE date=?").also { statement ->
                statement.setDate(1, java.sql.Date(flightDate.time))
            }
            updateCount += statement.executeUpdate()
        }
        return "Updated $updateCount flights"
    }

    /**
     * Удаляет планету с указанным идентификатором.
     * Пример: /delete_planet?planet_id=1
     *
     * Возвращает строку с количеством удаленных записей
     */
    fun handleDeletePlanet(planetId: Int): String {
        val deleteCount = withConnection(false) {
            it.prepareStatement("DELETE FROM Planet WHERE id=?").also { stmt ->
                stmt.setInt(1, planetId)
            }.executeUpdate()
        }
        return "Deleted $deleteCount planets"
    }

    fun handleDeletePlanetCorrected(planetId: Int): String {
        val deleteCount = withConnection(true) {
            it.prepareStatement("DELETE FROM Planet WHERE id=?").also { statement ->
                statement.setInt(1, planetId)
            }.executeUpdate()
        }
        return "Deleted $deleteCount planets"
    }


    /**
     * Returns a list of identifiers of flights departing at the specified date.
     */
    private fun cacheFlights(flightDate: Date?): List<Int> {
        val flightIds = mutableListOf<Int>()

        // Just get all needed flight identifiers
        withConnection(true) {
            // Build the query...
            if (flightDate == null) {
                it.prepareStatement("SELECT id FROM Flight")
            } else {
                it.prepareStatement("SELECT id FROM Flight WHERE date=?").also { stmt ->
                    stmt.setDate(1, java.sql.Date(flightDate.time))
                }
            }.use {
                // ... and execute it
                it.executeQuery().use { resultSet ->
                    // Voila, now let's make sure all the flights we need are cached to boost performance.
                    // Stupid database...
                    while (resultSet.next()) {
                        val flightId = resultSet.getInt("id")
                        if (!this.flightCache.containsKey(flightId)) {
                            // OMG, cache miss! Let's fetch this data
                            val flightEntity = entityManager.find(FlightEntity::class.java, flightId)
                            if (flightEntity != null) {
                                this.flightCache[flightId] = flightEntity
                            }
                        }
                        flightIds.add(flightId)
                    }
                }
            }
        }
        return flightIds
    }
}

private const val FLIGHTS_HEADER = """
        <html>
        <body>
        <style>
    table > * {
        text-align: left;
    }
    td {
        padding: 5px;
    }
    table { 
        border-spacing: 5px; 
        border: solid grey 1px;
        text-align: left;
    }
        </style>
        <table>
            <tr><th>Flight ID</th><th>Date</th><th>Planet</th><th>Planet ID</th></tr>
        """

private const val FLIGHTS_FOOTER = """
        </table>
        </body>
        </html>"""
