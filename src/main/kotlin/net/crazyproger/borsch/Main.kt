package net.crazyproger.borsch

import io.grpc.Server
import io.grpc.ServerBuilder
import net.crazyproger.borsch.entity.ItemTable
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.PlayerService
import net.crazyproger.borsch.rpc.player.PlayerGrpc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlin.sql.Database

class App {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(App::class.java)
        private var _database: Database by Delegates.notNull<Database>()
        val database: Database get() {
            return _database
        }
    }

    val port = 50051
    var server: Server? = null

    fun start() {

        initDb()
        startGrpc()

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            println("*** shutting down gRPC server since JVM is shutting down");
            stop();
            System.err.println("*** server shut down");
        });
    }

    private fun initDb() {
        Properties().apply {
            load(classpathStream("/database-test.properties") ?: classpathStream("/database.properties"))
            val database = Database.connect(getProperty("url"), getProperty("driver"), getProperty("user", ""), getProperty("password", ""))
            database.withSession {
                createMissingTablesAndColumns(PlayerTable, ItemType, ItemTable)
            }
            _database = database
        }
    }

    private fun classpathStream(path: String) = this@App.javaClass.getResourceAsStream(path)

    private fun startGrpc() {
        server = ServerBuilder.forPort(port)
                .addService(PlayerGrpc.bindService(PlayerService()))
                .build().start()
        log.info("Server started, listening on " + port)
    }

    fun stop() = server?.shutdown()

    fun blockUntilShutdown() = server?.awaitTermination()
}

fun main(args: Array<String>) {
    val app = App()
    app.start()
    app.blockUntilShutdown()
}