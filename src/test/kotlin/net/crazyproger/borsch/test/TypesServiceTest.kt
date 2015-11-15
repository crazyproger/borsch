package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.rpc.item.TypesServiceGrpc
import org.junit.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TypesServiceTest : AbstractAppTest() {

    var serviceBlockingStub by Delegates.notNull<TypesServiceGrpc.TypesServiceBlockingStub>()
    val count = 10

    override fun before() {
        super.before()
        serviceBlockingStub = TypesServiceGrpc.newBlockingStub(rawChannel)
        App.database.withSession {
            1.until(count + 1).forEach { ItemType.new { name = "type $it"; price = it } }
        }
    }

    @Test fun test_get_all_types() {
        val types = serviceBlockingStub.types(Empty.getDefaultInstance())
        assertNotNull(types.typeList)
        assertEquals(10, types.typeList.size)
        1.until(count + 1).forEach {
            val type = types.getType(it - 1)
            assertEquals("type $it", type.name)
            assertEquals(it, type.price)
        }
    }
}