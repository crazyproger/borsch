package net.crazyproger.borsch.test

import net.crazyproger.borsch.entity.ItemTable
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.entity.PlayerTable
import org.junit.Test
import kotlin.sql.exists
import kotlin.sql.tests.h2.DatabaseTestsBase
import kotlin.test.assertTrue

class SchemaTest : DatabaseTestsBase() {
    @Test fun test(){
        withTables(PlayerTable, ItemTable, ItemType){
            assertTrue(PlayerTable.exists())
            assertTrue(ItemTable.exists())
            assertTrue(ItemType.exists())
        }
    }
}