package io.javalin.plugin.socketio

import io.javalin.Javalin
import io.javalin.core.plugin.Plugin
import io.socket.engineio.server.EngineIoServer
import io.socket.engineio.server.EngineIoServerOptions
import io.socket.socketio.server.SocketIoServer

/**
 * Provides Socket.io server functionality within Javalin
 *
 * @author Joseph Dicarlo @josephdicarlo1
 */
class SocketIoPlugin(path: String = "/socket.io/*", handler: (SocketIoHandler) -> Unit): Plugin {

    private val path: String
    private val mEngineIoServer: EngineIoServer
    private val mSocketIoServer: SocketIoServer

    init {
        // Disable Cors handling by the engine.io server so Javalin can handle it (if configured, anyway)
        val options = EngineIoServerOptions.newFromDefault()

        options.isCorsHandlingDisabled = true

        mEngineIoServer = EngineIoServer(options)
        mSocketIoServer = SocketIoServer(mEngineIoServer)

        this.path = formatPath(path)

        handler.invoke(SocketIoHandler(mSocketIoServer))
    }

    /** Ensures the provided socket.io path is formatted correctly */
    private fun formatPath(path: String): String {
        val prefix = if (path.startsWith("/")) {""} else {"/"}
        val suffix = if (path.endsWith("/*")) {""} else {"/*"}
        return "$prefix$path$suffix"
    }

    override fun apply(app: Javalin) {
        app.get(path) { mEngineIoServer.handleRequest(it.req, it.res) }
        app.post(path) { mEngineIoServer.handleRequest(it.req, it.res) }
        app.ws(path) { SocketIoWsHandler(mEngineIoServer).apply(it) }
    }


}