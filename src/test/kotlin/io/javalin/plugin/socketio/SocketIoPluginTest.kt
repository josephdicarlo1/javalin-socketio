package io.javalin.plugin.socketio

import io.javalin.Javalin
import io.socket.client.IO
import io.socket.client.Socket
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.platform.commons.logging.LoggerFactory
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class SocketIoPluginTest {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun `plugin supports multiple namespaces`() {
        // Create and start a Javalin instance with two namespaces
        Javalin.create() { config ->
            config.registerPlugin(SocketIoPlugin() { io ->
                io.namespace("/foo") { socket ->
                    socket.on("fooping") {
                        logger.info { "got fooping message from client, sending foopong" }
                        socket.send("foopong")
                    }
                }
                io.namespace("/bar") { socket ->
                    socket.on("barping") {
                        logger.info { "got barping message from client, sending barpong" }
                        socket.send("barpong")
                    }
                }
            })
        }.events { event ->
            event.wsHandlerAdded { logger.info { "${it.path} ${it.handler} ${it.handlerType}" } }
            event.handlerAdded { logger.info { "${it.path} ${it.handler} ${it.httpMethod}" } }
        }.start(7000)

        // Specify the namespace in the client options and create the client
        val fooSocket: Socket = IO.socket(URI.create("ws://localhost:7000")).io().socket("/foo")

        val foopongReceived = CountDownLatch(1)

        fooSocket.on("foopong") {
            logger.info { "got foopong event from server" }
            foopongReceived.countDown()
        }

        logger.info { "trying to connect" }

        TestUtils.waitForSocketToConnect(fooSocket.open())

        logger.info { "Socket.io should be connected now..." }

        fooSocket.emit("fooping")

        logger.info { "fooping event sent, waiting for foopong" }

        try {
            foopongReceived.await(10, TimeUnit.SECONDS)
            if(foopongReceived.count > 0) fail("no reply received")
            logger.info { "foopong reply received" }
        } catch (x: InterruptedException) {
            fail(x)
        }

        // Specify the namespace in the client options and create the client
        val barSocket: Socket = IO.socket(URI.create("ws://localhost:7000")).io().socket("/bar")

        val barpongReceived = CountDownLatch(1)

        barSocket.on("barpong") {
            logger.info { "got barpong event from server" }
            barpongReceived.countDown()
        }

        logger.info { "trying to connect" }

        TestUtils.waitForSocketToConnect(barSocket.open())

        logger.info { "Socket.io should be connected now..." }

        barSocket.emit("barping")

        logger.info { "barping event sent, waiting for barpong" }

        try {
            barpongReceived.await(10, TimeUnit.SECONDS)
            if(barpongReceived.count > 0) fail("no reply received")
            logger.info { "barpong reply received" }
        } catch (x: InterruptedException) {
            fail(x)
        }
    }

    @Test
    fun `plugin should allow setting a custom path`() {
        // Create and start a Javalin instance with a SocketIoPlugin using a custom path
        Javalin.create() { config ->
            config.registerPlugin(SocketIoPlugin("/custompath/*") { io ->
                io.namespace("/") { socket ->
                    socket.on("ping") {
                        logger.info { "got ping message from client, sending pong" }
                        socket.send("pong")
                    }
                }
            })
        }.start(7001)

        // Specify the custom path in the client options and create the client
        val socket: Socket = IO.socket(URI.create("ws://localhost:7001"), IO.Options.builder()
            .setPath("/custompath")
            .build())

        val pongReceived = CountDownLatch(1)

        socket.on("pong") {
            logger.info { "got pong event from server" }
            pongReceived.countDown()
        }

        logger.info { "trying to connect" }

        TestUtils.waitForSocketToConnect(socket.open())

        logger.info { "Socket.io should be connected now..." }

        socket.emit("ping")

        logger.info { "ping event sent, waiting for pong" }

        try {
            pongReceived.await(10, TimeUnit.SECONDS)
            assertEquals(0, pongReceived.count)
            logger.info { "pong reply received" }
        } catch (x: InterruptedException) {
            fail(x)
        }
    }

    @Test
    fun `server should receive query params from client`() {
        // Create and start the Javalin server with the Socket.io plugin
        Javalin.create() {
            it.registerPlugin(SocketIoPlugin() { io ->
                io.namespace("/") { socket ->
                    val query = socket.initialQuery["myParam"]
                    socket.send("hello", query)
                }
            })
        }.start(7003)

        // Specify the custom path in the client options and create the client
        val socket: Socket = IO.socket(URI.create("ws://localhost:7003"), IO.Options.builder()
            .setQuery("myParam=42")
            .build())

        val latch = CountDownLatch(1)

        // When the "hello" event is received, log it
        socket.on("hello") {
            val myParam = it[0]
            logger.info { "received hello event: myParam value is $myParam" }
            assertEquals("42", myParam)
            latch.countDown()
        }

        logger.info { "Trying to connect now..." }

        // Connect the client to Javalin
        TestUtils.waitForSocketToConnect(socket.open())

        latch.await(10, TimeUnit.SECONDS)

        assertEquals(0, latch.count)
    }

    @Test
    fun `client should connect to socket io using http`() {
        // Create and start the Javalin server with the Socket.io plugin
        Javalin.create() {
            logger.info { "Registering plugins" }
            it.registerPlugin(SocketIoPlugin() { io ->
                io.namespace("/") { socket ->
                    socket.send("hello", "world")
                }
            })
        }.start(7002)

        // Specify the custom path in the client options and create the client
        val socket: Socket = IO.socket(URI.create("http://localhost:7002"))

        val latch = CountDownLatch(1)

        // When the "hello" event is received, log it
        socket.on("hello") {
            logger.info { "received hello event" }
            latch.countDown()
        }

        logger.info { "Trying to connect now..." }

        // Connect the client to Javalin
        TestUtils.waitForSocketToConnect(socket.open())

        latch.await(10, TimeUnit.SECONDS)

        assertEquals(0, latch.count)
    }

}