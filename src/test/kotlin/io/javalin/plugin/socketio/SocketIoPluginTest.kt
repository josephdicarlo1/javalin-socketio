package io.javalin.plugin.socketio

import io.javalin.Javalin
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.socketio.server.SocketIoSocket
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class SocketIoPluginTest {

    private val logger = KotlinLogging.logger {}

    @Test
    fun `plugin should allow setting a custom path`() {

        // Define a plugin with a custom path that emits a "hello" event on connection
        val plugin = SocketIoPlugin("/custompath/*") { io ->
            io.namespace("/")
                .on("connection") {
                    val socket = it[0] as SocketIoSocket
                    socket.emit("hello", "world")
                }
        }

        // Create and start the Javalin server using the plugin
        val app = Javalin.create() {
            it.registerPlugin(plugin)
        }.start(7000)

        // Specify the custom path in the client options and create the client
        val socket: Socket = IO.socket(URI.create("https://localhost:7000"), IO.Options.builder()
            .setPath("/custompath")
            .build())

        // When the "hello" event is received, log it
        socket.io()
            .on("hello") {
                logger.info { "received hello event" }
            }

        // Connect the client to Javalin
        socket.open()

        // Ensure that the client actually connected
        assertThat(socket.connected()).isTrue

    }

    @Test
    fun `server should receive query params from client`() {

        val options = IO.Options.builder()
            .setPath("/socket.io")
            .setQuery("x=42")
            .build()

        val socket: Socket = IO.socket(URI.create("https://localhost:7000"), options)
    }

    @Test
    fun `client should connect to socket io using http`() {
        val options = IO.Options.builder()
            .setPath("/socket.io")
            .build()

        val socket: Socket = IO.socket(URI.create("https://localhost:7000"), options)
    }

}