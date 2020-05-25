package com.svacina.hw.router

import com.svacina.hw.handler.ScanHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter


@Configuration
class ScanRouter {

    @Bean
    fun scan(scanHandler: ScanHandler): RouterFunction<ServerResponse> = coRouter {
        POST("/scan", scanHandler::scan)
    }

    @Bean
    fun homepage(scanHandler: ScanHandler): RouterFunction<ServerResponse> = coRouter {
        GET("/", scanHandler::homepage)
    }
}