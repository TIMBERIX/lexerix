@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.dao.id.IntIdTable

object Companies : IntIdTable("FK_Firma", "lID_Firma") {
    val bBrutto = bool("bBrutto")
}