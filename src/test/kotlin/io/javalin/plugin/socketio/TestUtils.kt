package io.javalin.plugin.socketio

import io.socket.client.Socket

object TestUtils {
    fun waitForSocketToConnect(socket: Socket) {
        var waits = 0

        while(!socket.connected()) {
            Thread.sleep(1000)
            if(waits == 10) {
                throw Exception("socket.io never connected after 10 seconds")
            } else {
                waits++
            }
        }
    }

}