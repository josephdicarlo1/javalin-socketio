package io.javalin.plugin.socketio

import io.socket.socketio.server.SocketIoServer
import io.socket.socketio.server.SocketIoSocket

typealias SocketIoNamespaceHandler = (socket: SocketIoSocket) -> Unit

/**
 * Provides Socket.io server functionality within Javalin
 *
 * @author Joseph Dicarlo @josephdicarlo1
 */
class SocketIoHandler(private val socketIoServer: SocketIoServer) {
    fun namespace(path: String, handler: SocketIoNamespaceHandler) {
        socketIoServer.namespace(path).on("connection") {
            handler.invoke((it[0] as SocketIoSocket))
        }
    }
}