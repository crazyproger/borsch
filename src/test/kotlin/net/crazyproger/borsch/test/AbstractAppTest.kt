package net.crazyproger.borsch.test

import io.grpc.Channel
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannelBuilder
import net.crazyproger.borsch.App
import net.crazyproger.borsch.createTables
import net.crazyproger.borsch.entity.TABLES
import net.crazyproger.borsch.rpc.BusinessErrors
import net.crazyproger.borsch.rpc.BusinessException
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import java.util.*
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.fail

abstract class AbstractAppTest {
    var secretKey by Delegates.notNull<String>();
    var withIdentityChannel  by Delegates.notNull<Channel>()

    @Before open fun before() {
        secretKey = UUID.randomUUID().toString()
        withIdentityChannel = ClientInterceptors.intercept(rawChannel, ClientIdentificationInterceptor(secretKey))
        createTables(App.database)
    }

    @After fun after() {
        App.database.transaction {
            drop(*TABLES)
        }
    }

    companion object {
        var app: App? = null
        var rawChannel by Delegates.notNull<Channel>()
        @JvmStatic @BeforeClass fun beforeClass() {
            app = App().apply { start() }
            rawChannel = ManagedChannelBuilder.forAddress("localhost", app!!.port)
                    .usePlaintext(true).build()
        }

        @JvmStatic @AfterClass fun afterClass() {
            app?.stop()
            app?.blockUntilShutdown()
        }
    }
}

fun assertBusinessError(expected: BusinessErrors.Error, case: () -> Unit) {
    try {
        case()
        fail("case completed, but should not")
    } catch (e: BusinessException) {
        assertEquals(e.protoError, expected)
    }
}
