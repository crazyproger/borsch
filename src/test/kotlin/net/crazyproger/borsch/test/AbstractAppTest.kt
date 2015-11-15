package net.crazyproger.borsch.test

import io.grpc.Channel
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannelBuilder
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.TABLES
import org.junit.After
import org.junit.Before
import java.util.*
import kotlin.properties.Delegates

abstract class AbstractAppTest {
    var app: App? = null
    var rawChannel by Delegates.notNull<Channel>()
    var secretKey by Delegates.notNull<String>();
    var withIdentityChannel  by Delegates.notNull<Channel>()

    @Before open fun before() {
        // todo should be executed one time for whole suite
        app = App().apply { start() }
        rawChannel = ManagedChannelBuilder.forAddress("localhost", app!!.port)
                .usePlaintext(true).build()

        secretKey = UUID.randomUUID().toString()
        withIdentityChannel = ClientInterceptors.intercept(rawChannel, ClientIdentificationInterceptor(secretKey))
    }

    @After fun after() {
        app?.stop()
        app?.blockUntilShutdown()
        App.database.withSession {
            drop(*TABLES)
        }
    }
}