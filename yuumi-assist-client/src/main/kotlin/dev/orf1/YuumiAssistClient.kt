package dev.orf1

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.runBlocking
import java.awt.Dimension
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.KeyEvent
import kotlin.math.roundToInt

fun main() {
    println("Please enter host: (ex 192.168.1.83)")
    val h = readLine()
    println("Starting client.")
    val client = HttpClient {
        install(WebSockets)
    }
    runBlocking {
        if (h != null) {
            client.webSocket(method = HttpMethod.Get, host = h, port = 8080, path = "/socket") {
                while (true) {
                    val incoming = incoming.receive() as? Frame.Text ?: continue
                    var msg = incoming.readText();
                    println(msg)

                    when (msg) {
                        "Q" -> {
                            Robot().keyPress(KeyEvent.VK_Q)
                            Robot().keyRelease(KeyEvent.VK_Q)

                        }
                        "W" -> {
                            Robot().keyPress(KeyEvent.VK_W)
                            Robot().keyRelease(KeyEvent.VK_W)
                        }
                        "E" -> {
                            Robot().keyPress(KeyEvent.VK_E)
                            Robot().keyRelease(KeyEvent.VK_E)
                        }
                        "R" -> {
                            Robot().keyPress(KeyEvent.VK_R)
                            Robot().keyRelease(KeyEvent.VK_R)
                        }
                        "D" -> {
                            Robot().keyPress(KeyEvent.VK_D)
                            Robot().keyRelease(KeyEvent.VK_D)
                        }
                        "F" -> {
                            Robot().keyPress(KeyEvent.VK_F)
                            Robot().keyRelease(KeyEvent.VK_F)
                        }
                        else -> {
                            if (msg.startsWith("M:")) {
                                msg = msg.replace("M:", "")
                                val values = msg.split("%")
                                val xRaw = values[0].toFloat()
                                val yRaw = values[1].toFloat()

                                val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
                                val xScaled: Float = screenSize.width.toFloat() * (xRaw.toFloat() / 1000F)
                                val yScaled: Float = screenSize.height.toFloat() * (yRaw.toFloat() / 1000F)
                                Robot().mouseMove(xScaled.roundToInt(), yScaled.roundToInt())
                                println("Moved mouse to ${xScaled.roundToInt()} ${yScaled.roundToInt()}")
                            }
                        }
                    }
                }
            }
        }
    }
}


