package io.javalin.plugin.socketio

import io.socket.engineio.server.EngineIoServer
import io.socket.engineio.server.EngineIoWebSocket
import io.socket.engineio.server.utils.ParseQS
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketListener
import java.nio.ByteBuffer

/**
 * Adapter for Javalin WebSocket implementation
 *
 * Adapted from [engine.io-server-java](https://github.com/socketio/engine.io-server-java/tree/master/engine.io-server-jetty)
 *
 * @author Joseph Dicarlo @josephdicarlo1
 */
class JavalinEngineIoWebSocketAdapter(private val mServer: EngineIoServer): EngineIoWebSocket(), WebSocketListener {

    private var mSession: Session? = null
    private var mQuery: MutableMap<String, String>? = null
    private var mHeaders: MutableMap<String, MutableList<String>>? = null

    override fun getQuery(): MutableMap<String, String>? {
        return mQuery
    }

    override fun getConnectionHeaders(): MutableMap<String, MutableList<String>>? {
        return mHeaders
    }

    override fun write(message: String?) {
        mSession?.remote?.sendString(message)
    }

    override fun write(message: ByteArray?) {
        mSession?.remote?.sendBytes(ByteBuffer.wrap(message))
    }

    override fun close() {
        mSession!!.close()
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        emit("close")
        mSession = null
    }

    override fun onWebSocketConnect(session: Session) {
        mSession = session
        mQuery = ParseQS.decode(session.upgradeRequest.queryString)
        mHeaders = session.upgradeRequest.headers

        mServer.handleWebSocket(this)
    }

    override fun onWebSocketError(cause: Throwable?) {
        emit("error", "write error", cause?.message)
    }

    override fun onWebSocketBinary(payload: ByteArray, offset: Int, len: Int) {
        val message: ByteArray
        if (offset == 0 && len == payload.size) {
            message = payload
        } else {
            message = ByteArray(len)
            System.arraycopy(payload, offset, message, 0, len)
        }

        emit("message", message as Any)
    }

    override fun onWebSocketText(message: String) {
        emit("message", message as Any)
    }
}