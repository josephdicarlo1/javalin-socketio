package io.javalin.plugin.socketio

import io.javalin.websocket.*
import io.socket.engineio.server.EngineIoServer

class SocketIoWsHandler(mEngineIo: EngineIoServer):
    WsConnectHandler,
    WsMessageHandler,
    WsBinaryMessageHandler,
    WsCloseHandler,
    WsErrorHandler {

    private var engineIoWs: JavalinEngineIoWebSocketAdapter = JavalinEngineIoWebSocketAdapter(mEngineIo)

    fun apply(wsConfig: WsConfig) {
        wsConfig.onConnect(this)
        wsConfig.onMessage(this)
        wsConfig.onBinaryMessage(this)
        wsConfig.onClose(this)
        wsConfig.onError(this)
    }

    override fun handleBinaryMessage(ctx: WsBinaryMessageContext) {
        engineIoWs.onWebSocketBinary(ctx.data(), ctx.offset(), ctx.length())
    }

    override fun handleClose(ctx: WsCloseContext) {
        engineIoWs.onWebSocketClose(ctx.status(), ctx.reason())
    }

    override fun handleConnect(ctx: WsConnectContext) {
        engineIoWs.onWebSocketConnect(ctx.session)
    }

    override fun handleError(ctx: WsErrorContext) {
        engineIoWs.onWebSocketError(ctx.error())
    }

    override fun handleMessage(ctx: WsMessageContext) {
        engineIoWs.onWebSocketText(ctx.message())
    }
}