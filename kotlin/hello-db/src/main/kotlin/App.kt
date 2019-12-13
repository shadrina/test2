// Copyright (C) 2019 Dmitry Barashev
package hellodb

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import spark.Spark.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Это просто веб сервер с несколькими обработчиками запросов, которые
 * проверяют входные параметры и приводят их из строки к нужным типам.
 *
 * В этом коде грабли можно поискать, но только в качестве бонуса.
 */
class App {
  private val handler = FlightsHandler()

  init {
    exception(Exception::class.java) { e, _, _ ->
      e.printStackTrace()
    }
    staticFiles.location("/public")
    port(8080)

    get("/") { req, res ->
      "Привет. Тебе интересно сходить на /flights, /delete_planet или /delay_flights"
    }

    get("/flights") { req, res ->
      res.header("Content-type", "text/html;charset=utf-8");
      val flightDate: Date? = req.queryParams("flight_date")?.let {
        SimpleDateFormat("yyyy-MM-dd").parse(it)
      }
      handler.handleFlightsCorrected(flightDate)
    }

    get("/delay_flights") { req, res ->
      val flightDate: Date? = req.queryParams("flight_date")?.let {
        SimpleDateFormat("yyyy-MM-dd").parse(it)
      }
      val interval: String ? = req.queryParams("interval")
      if (flightDate == null || interval == null) {
        "Please specify flight_date and interval arguments, like this: /delay_flights?flight_date=2084-06-12&interval=1week"
      } else {
        handler.handleDelayFlightsCorrected(flightDate, interval)
      }
    }

    get("/delete_planet") { req, res ->
      val planetId: Int? = req.queryParams("planet_id")?.toIntOrNull()
      if (planetId == null) {
        "Please specify planet_id, like this: /delete_planet?planet_id=1"
      } else {
        handler.handleDeletePlanetCorrected(planetId)
      }
    }
  }

}

class RakesCli : CliktCommand() {
  private val pgUser: String by option("--pg-user").default("postgres")
  private val pgHost: String by option("--pg-host").default("localhost")
  private val pgPort: Int by option("--pg-port").int().default(5432)
  private val pgDatabase: String by option("--pg-database").default("postgres")
  private val pgPassword: String by option("--pg-password").default("postgres")

  override fun run() {
    initDb(host = pgHost, port = pgPort, database = pgDatabase, user = pgUser, password = pgPassword)
    App()
  }
}

fun main(args: Array<String>) = RakesCli().main(args)
