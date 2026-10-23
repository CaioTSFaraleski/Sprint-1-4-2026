package br.com.safefield.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AlertasWebSocketHandler handler;

    public WebSocketConfig(AlertasWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // canal só de leitura (não recebe comandos), por isso aceita qualquer origem
        registry.addHandler(handler, "/ws/alertas").setAllowedOriginPatterns("*");
    }
}
