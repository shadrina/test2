package hellodb

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.DriverManager
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence


lateinit var url: String
lateinit var dataSource: HikariDataSource
private lateinit var emf: EntityManagerFactory
lateinit var entityManager: EntityManager

fun initDb(user: String = "postgres", password: String = "", database: String = "postgres", host: String = "localhost", port: Int = 5432) {
  url = "jdbc:postgresql://$host:$port/$database?user=$user&defaultAutoCommit=false&password=$password"
  dataSource = HikariDataSource().apply {
    username = user
    jdbcUrl = "jdbc:postgresql://$host:$port/$database"
    this.password = password
    maximumPoolSize = 10
  }
  emf = Persistence.createEntityManagerFactory("Postgres",
    mapOf("javax.persistence.jdbc.url" to url,
        "javax.persistence.jdbc.user"  to user,
        "javax.persistence.jdbc.password" to password)
  )
  entityManager = emf.createEntityManager()
}

fun <T> withConnection(hikari: Boolean, code: (Connection) -> T) : T {
  return if (hikari) dataSource.connection.use(code) else code(getconn())
}

fun getconn(): Connection {
  return DriverManager.getConnection(url).also {
    it.autoCommit = false
  }
}
