package dev.orf1

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Toolkit
import javax.swing.*
import kotlin.system.exitProcess


fun main() {
    YuumiAssistServer().start()
}

class YuumiAssistServer : NativeKeyListener, NativeMouseMotionListener {
    private lateinit var connection: DefaultWebSocketServerSession
    private var connected = false
    private var busy = false

    fun start() {
        overlay()
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            install(WebSockets)
            routing {
                webSocket("/socket") {
                    println("Client connected!")
                    connection = this
                    connected = true

                    registerHooks()

                    try {
                        for (frame in incoming) {
                            println(frame.readBytes())
                            if (frame is Frame.Text) {
                                val receivedText = frame.readText()
                                println("onMessage $receivedText")
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        connected = false
                        println("onClose ${closeReason.await()}")
                    } catch (e: Throwable) {
                        connected = false
                        println("onError ${closeReason.await()}")
                        e.printStackTrace()
                    }
                }
            }
        }.start(wait = true)
    }

    private fun overlay() {
        val frame = JFrame("Yuumi Assist Server")
        val panel = JPanel()
        frame.isUndecorated = true;
        panel.layout = FlowLayout()

        panel.add(JLabel("Yuumi Assist Options"), BorderLayout.LINE_START)
        panel.add(JButton("Start Server"), BorderLayout.LINE_START)
        panel.add(JCheckBox("Auto Upgrade Abilities"), BorderLayout.LINE_START)
        panel.add(JCheckBox("Auto Dodge"), BorderLayout.LINE_START)
        panel.add(JCheckBox("Mouse Control"), BorderLayout.LINE_START)


        frame.add(panel)
        frame.setSize(200, 300)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }


    override fun nativeKeyPressed(e: NativeKeyEvent) {
        println("Key Pressed: " + NativeKeyEvent.getKeyText(e.keyCode))
        if(connected) {
            runBlocking {
                if (NativeKeyEvent.getKeyText(e.keyCode).equals("Insert")) {
                    println("Sending Q.")
                    connection.send("Q")
                } else if (NativeKeyEvent.getKeyText(e.keyCode).equals("Home")) {
                    println("Sending W.")
                    connection.send("W")
                } else if (NativeKeyEvent.getKeyText(e.keyCode).equals("Page Up")) {
                    println("Sending E.")
                    connection.send("E")
                } else if (NativeKeyEvent.getKeyText(e.keyCode).equals("Delete")) {
                    println("Sending R.")
                    connection.send("R")
                } else if (NativeKeyEvent.getKeyText(e.keyCode).equals("End")) {
                    println("Sending D.")
                    connection.send("D")
                } else if (NativeKeyEvent.getKeyText(e.keyCode).equals("Page Down")) {
                    println("Sending F.")
                    connection.send("F")
                }
            }
        }
    }

    override fun nativeMouseMoved(e: NativeMouseEvent) {
        if(connected) {
            if (busy) {
                return
            }
            busy = true
            runBlocking {
                val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
                val xScaled: Float = 1000F * (e.x.toFloat() / screenSize.width.toFloat())
                val yScaled: Float = 1000F * (e.y.toFloat() / screenSize.height.toFloat())
                connection.send("M:${xScaled}%${yScaled}")
                println("Sending M:${xScaled}%${yScaled}")

            }
            Thread {
                Thread.sleep(50)
                busy = false
            }.start()
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        println("Key Released: " + NativeKeyEvent.getKeyText(e.keyCode))
    }

    private fun registerHooks() {
        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            System.err.println("There was a problem registering the native hook.")
            System.err.println(ex.message)
            exitProcess(1)
        }
        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseMotionListener(this);
    }
}

