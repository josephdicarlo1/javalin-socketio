package io.javalin.plugin.socketio

import io.javalin.Javalin
import io.javalin.core.util.CorsPlugin
import io.javalin.core.util.RouteOverviewPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("TestMain")

/**
 * Runnable "application" for manually testing socket.io plugin using
 * [Socket.io Client Tool](https://amritb.github.io/socketio-client-tool/)
 */
fun main() {

    val plugin = SocketIoPlugin() { io ->
        io.namespace("/") {
            logger.info("Connected!")
            it.send("message", "hello world")
        }
        io.namespace("/blah") { socket ->
            socket.on("socketio-client") { args ->
                logger.info("socketio-client event")
                logger.info(args.toString())
                socket.send("message", "socketio-client event")
            }

            socket.on("message") { args ->
                logger.info("message event")
                logger.info(args.toString())
                socket.send("message", "message event")
            }
        }
    }

    // Create and start the Javalin server using the plugin
    Javalin.create() {
        it.registerPlugin(plugin)
        it.registerPlugin(CorsPlugin(listOf("https://amritb.github.io")))
        it.registerPlugin(RouteOverviewPlugin("/routes"))
    }.start(7000)
}