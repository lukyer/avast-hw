package com.svacina.hw.router

import com.svacina.hw.handler.ScanHandler
import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*


@Configuration
class ScanRouter {

    @Bean
    fun scan(scanHandler: ScanHandler):RouterFunction<ServerResponse> = coRouter {
        POST("/scan", scanHandler::scan)
    }

    @Bean
    fun homepage(scanHandler: ScanHandler):RouterFunction<ServerResponse> = coRouter {
        GET("/", scanHandler::homepage)
    }
}