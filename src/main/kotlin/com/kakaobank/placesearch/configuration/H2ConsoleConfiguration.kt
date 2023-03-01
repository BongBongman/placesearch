package com.kakaobank.placesearch.configuration

import com.kakaobank.placesearch.log
import org.h2.tools.Server
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import java.sql.SQLException

@Configuration
class H2ConsoleConfiguration {
    private val consoleServer = Server.createWebServer()
    @EventListener(ContextRefreshedEvent::class)
    @Throws(SQLException::class)
    fun start() {
        log().info("start h2 console")
        consoleServer.start()
    }

    @EventListener(ContextClosedEvent::class)
    fun stop() {
        log().info("stop h2 console")
        consoleServer.stop()
    }
}