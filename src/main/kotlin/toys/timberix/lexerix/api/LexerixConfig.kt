package toys.timberix.lexerix.api

import kotlinx.serialization.Serializable

@Serializable
data class LexerixConfig(
    val company: String,
    val ip: String,
    val user: String,
    val password: String,
    val port: Int = 2638,
    val databaseFolder: String = "C:\\ProgramData\\Lexware\\professional\\Datenbank",
)