@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.utils

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

open class DatedTable(name: String) : Table(name) {
    /** gmt timestamp of creation */
    val created = timestamp("System_created").clientDefault { Clock.System.now() }.nullable()

    /** gmt timestamp of last update */
    val lastUpdated = timestamp("System_updated").clientDefault { Clock.System.now() }

    /** user that created this value initially */
    val createdUser = varchar("System_created_user", 255).default("LEXERIX")

    /** user that last updated this value */
    val updatedUser = varchar("System_updated_user", 255).default("LEXERIX")
}
