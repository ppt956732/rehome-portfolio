package com.rehome.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 👈 這一行就是開啟 WebSocket 的關鍵開關
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 設定連線端點 (Endpoint)
        // 前端寫 new SockJS('http://localhost:8081/ws') 就是連這裡
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 👈 允許跨域 (解決你剛剛連線失敗的主因)
                .withSockJS(); // 啟用 SockJS 支援
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 設定訊息代理 (Broker)
        // "/topic" 用於廣播 (群聊)
        // "/queue" 或 "/user" 用於點對點 (私聊)
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        
        // 3. 設定前端發送訊息的前綴
        // 前端 stompClient.send("/app/chat", ...)
        registry.setApplicationDestinationPrefixes("/app");
        
        // 4. 設定點對點使用者的前綴
        registry.setUserDestinationPrefix("/user");
    }
}