@file:Suppress("unused")

package toys.timberix.lexerix.api

import com.sybase.jdbc4.jdbc.SybDriver
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.SQLServerDialect
import java.sql.SQLException
import java.util.logging.Logger

internal val logger = Logger.getLogger("LEXERIX")

/**
 * This class is the main entry point for the Lexerix API.
 */
class Lexerix {
    private var _db: Database? = null
    private val db: Database
        get() {
            if (_db == null) throw RuntimeException("Database not connected")
            return _db!!
        }

    /**
     * Connect to the Lexware sybase database and start the company database if necessary.
     *
     * If there is already a connection, it will be closed and a new one will be opened.
     *
     * @param company The company id/path, e.g. F1, F2, etc.
     * @param databaseFolder The folder where all Lexware databases are located.
     */
    fun connect(
        company: String,
        ip: String,
        user: String,
        password: String,
        port: Int = 2638,
        databaseFolder: String = "C:\\ProgramData\\Lexware\\professional\\Datenbank",
    ) {
        val driver = SybDriver::class.qualifiedName!!
        Database.registerJdbcDriver("jdbc:sybase", driver, SQLServerDialect.dialectName)

        // Try to connect to company db
        connectToCompany(company, ip, port, user, password, databaseFolder)
    }

    fun connect(
        config: LexerixConfig
    ) = connect(
        config.company,
        config.ip,
        config.user,
        config.password,
        config.port,
        config.databaseFolder
    )

    private fun startCompanyDatabase(name: String, databaseFolder: String) {
        logger.fine("Starting company database '$name'...")
        transaction {
            try {
                exec("START DATABASE '$databaseFolder\\$name\\LxCompany.db' AS $name")
            } catch (e: ExposedSQLException) {
                if (e.sqlState == "08W27") {
                    // "Database name not unique"
                    logger.severe("Company database '$name' already running. This should not happen!")
                }
                throw e
            }
        }
        logger.fine("Started company database '$name'")
    }

    /**
     * A recursive function which goes through the following loop a maximum of two times:
     *
     * 1. tries to connect to the company database    <---
     * 2. if the database is not found:                  |
     *        ( only tries the below once!)              |
     *   a. connects to the main database                |
     *   b. starts the company database                  |
     * 3. retries to connect to the company database  ---
     */
    private fun connectToCompany(
        company: String,
        ip: String,
        port: Int,
        user: String,
        password: String,
        databaseFolder: String,
        maybeStart: Boolean = true
    ) {
        fun dataSource(suffix: String = "") = HikariDataSource().apply {
            jdbcUrl = "jdbc:sybase:Tds:$ip:$port$suffix"
            username = user
            setPassword(password)
            driverClassName = SybDriver::class.qualifiedName!!
            connectionTestQuery = "SELECT 1"
            maximumPoolSize = 1 // 1 idle connection -> prevent the database from stopping!
        }

        _db?.let(TransactionManager::closeAndUnregister)
        _db = Database.connect(dataSource("?SERVICENAME=$company"))
        try {
            logger.fine("Testing connection to company database...")
            transaction {
                exec("SELECT 1")
            }
            logger.fine("Connection to company database functional")
        } catch (e: SQLException) {
            logger.fine("Connection failed with sql state ${e.sqlState}")

            val sqlState = e.sqlState
            if (sqlState == "JZ00L" || sqlState == "08004") {
                if (!maybeStart) {
                    logger.severe("Company database '$company' not found after starting!")
                    throw e
                }

                // Database not found
                // 1. connect to main database
                // 2. start company database
                // 3. retry to connect to company database
                TransactionManager.closeAndUnregister(db)
                _db = Database.connect(dataSource())
                startCompanyDatabase(company, databaseFolder)

                connectToCompany(company, ip, port, user, password, databaseFolder, maybeStart = false)
            } else {
                logger.severe("Unknown error while connecting to company database: ${e.message} (sql state: ${e.sqlState})")
                e.nextException?.let { nextE ->
                    logger.severe("Underlying/next exception:")
                    nextE.printStackTrace()
                }
                throw e
            }
        }
    }
}