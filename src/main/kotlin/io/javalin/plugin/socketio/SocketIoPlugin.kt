package io.javalin.plugin.socketio

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.plugin.Plugin
import io.socket.engineio.server.EngineIoServer
import io.socket.engineio.server.EngineIoServerOptions
import io.socket.socketio.server.SocketIoServer

typealias SocketIoHandler = (socketIoServer: SocketIoServer) -> Unit

/**
 * Provides Socket.io server functionality within Javalin
 *
 * @author Joseph Dicarlo (GitHub: @josephdicarlo1, Email: dic@rlo.io)
 */
class SocketIoPlugin(path: String = "/socket.io/*", handler: SocketIoHandler): Plugin {

    private val path: String
    private val mEngineIoServer: EngineIoServer
    private val mSocketIoServer: SocketIoServer

    init {

        val options = EngineIoServerOptions.newFromDefault()

        // Disable Cors handling by the engine.io server so Javalin can handle it (if configured)
        options.isCorsHandlingDisabled = true

        mEngineIoServer = EngineIoServer(options)

        mSocketIoServer = SocketIoServer(mEngineIoServer)

        this.path = formatPath(path)

        handler.apply { mSocketIoServer }
    }

    private fun formatPath(path: String): String {
        val prefix = if (path.startsWith("/")) {""} else {"/"}
        val suffix = if (path.endsWith("/*")) {""} else {"/*"}
        return "$prefix$path$suffix"
    }

    override fun apply(app: Javalin) {

        val engineIoWs = JavalinEngineIoWebSocket(mEngineIoServer)

        app.routes {
            path(path) {
                get {
                    mEngineIoServer.handleRequest(it.req, it.res)
                }
                post {
                    mEngineIoServer.handleRequest(it.req, it.res)
                }
            }

            ws(path) { ws ->
                ws.onConnect { engineIoWs.onWebSocketConnect(it.session) }
                ws.onMessage { engineIoWs.onWebSocketText(it.message()) }
                ws.onBinaryMessage { engineIoWs.onWebSocketBinary(it.data(), it.offset(), it.length()) }
                ws.onClose { engineIoWs.onWebSocketClose(it.status(), it.reason()) }
                ws.onError { engineIoWs.onWebSocketError(it.error()) }
            }
        }
    }


}