package net.crazyproger.borsch

import  io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import net.crazyproger.borsch.entity.TABLES
import net.crazyproger.borsch.rpc.BusinessExceptionInterceptor
import net.crazyproger.borsch.rpc.IdentificationInterceptor
import net.crazyproger.borsch.rpc.LoggingInterceptor
import net.crazyproger.borsch.rpc.item.ItemsServiceGrpc
import net.crazyproger.borsch.rpc.item.TypesServiceGrpc
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import net.crazyproger.borsch.rpc.service.*
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates

private val log: Logger = LoggerFactory.getLogger("main")

class App {
    companion object {
        private var _database: Database by Delegates.notNull<Database>()
        val database: Database get() {
            return _database
        }
    }

    val port = 50051
    var server: Server? = null

    fun start() {

        val database = initDb()
        val config = Config(database)
        startGrpc(database, config)
        _database = database

        registerShutdownHook()
    }

    private fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            println("*** shutting down gRPC server since JVM is shutting down");
            stop();
            System.err.println("*** server shut down");
        });
    }

    private fun initDb(): Database {
        operator fun Properties.get(key: String) = this.getProperty(key)
        fun classpathStream(path: String) = this@App.javaClass.getResourceAsStream(path)

        val properties = Properties().apply { load(classpathStream("/database-test.properties") ?: classpathStream("/database.properties")) }
        val database = Database.connect(properties["url"], properties["driver"], properties["user"] ?: "", properties["password"] ?: "")
        // todo do not create tables on production start(migrations?)
        createTables(database)
        return database
    }

    private fun startGrpc(database: Database, config: Config) {
        val createDef = ServerInterceptors.intercept(ProfileCreateServiceGrpc.bindService(ProfileCreateServiceImpl(database, config))
                , *defaultInterceptors)
        val playerDef = ServerInterceptors.intercept(PlayerServiceGrpc.bindService(PlayerServiceImpl(database))
                , IdentificationInterceptor(database), *defaultInterceptors)
        val itemsDef = ServerInterceptors.intercept(ItemsServiceGrpc.bindService(ItemsServiceImpl(database))
                , IdentificationInterceptor(database), *defaultInterceptors)
        val typesDef = ServerInterceptors.intercept(TypesServiceGrpc.bindService(TypesServiceImpl(database))
                , *defaultInterceptors)
        server = ServerBuilder.forPort(port)
                .addService(createDef)
                .addService(playerDef)
                .addService(typesDef)
                .addService(itemsDef)
                .build().start()
        log.info("Server started, listening on " + port)
    }

    private val defaultInterceptors = arrayOf(BusinessExceptionInterceptor, LoggingInterceptor)

    fun stop() = server?.shutdown()

    fun blockUntilShutdown() = server?.awaitTermination()
}

fun createTables(database: Database) {
    try {
        database.transaction {
            createMissingTablesAndColumns(*TABLES)
        }
    } catch(e: SQLException) {
        log.error("on create tables ", e) // SQLException when indexes are already exists
    }
}

fun main(args: Array<String>) {
    val app = App()
    app.start()
    app.blockUntilShutdown()
}
