# javalin-socketio

Socket.io plugin for [Javalin](https://github.com/tipsy/javalin)

## Usage

### Default path
```kotlin
Javalin.create() { config ->
    // Optional custom path
    // config.registerPlugin(SocketIoPlugin(mypath) { io ->
    config.registerPlugin(SocketIoPlugin() { io ->
        io.namespace("/") { socket ->
            // Get query params from initial connection
            val query = socket.initialQuery["myParam"] // -> myValue

            // send "hello" on connect
            socket.send("hello")

            // send "pong" on "ping" event
            socket.on("ping") {
                socket.send("pong")
            }
        }
        // Define multiple namespaces
        io.namespace("/foo") { socket -> 
            socket.send("hello from foo")
        }
    })
}.start(7000)
```