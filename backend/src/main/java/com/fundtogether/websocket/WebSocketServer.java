package com.fundtogether.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws/{userId}", configurator = WebSocketAuthConfig.class)
@Component
@Slf4j
public class WebSocketServer {

    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Boolean authenticated = (Boolean) session.getUserProperties().get("authenticated");
        if (authenticated == null || !authenticated) {
            log.warn("WebSocket连接被拒绝: 未认证用户, userId={}", userId);
            try {
                session.close(new jakarta.websocket.CloseReason(
                        jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required"));
            } catch (IOException e) {
                log.error("关闭WebSocket连接失败", e);
            }
            return;
        }

        Long tokenUserId = (Long) session.getUserProperties().get("userId");
        if (!String.valueOf(tokenUserId).equals(userId)) {
            log.warn("WebSocket连接被拒绝: userId不匹配, pathUserId={}, tokenUserId={}", userId, tokenUserId);
            try {
                session.close(new jakarta.websocket.CloseReason(
                        jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY, "User ID mismatch"));
            } catch (IOException e) {
                log.error("关闭WebSocket连接失败", e);
            }
            return;
        }

        sessionMap.put(userId, session);
        log.info("WebSocket connected for user: {}, Total connections: {}", userId, sessionMap.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionMap.remove(userId);
        log.info("WebSocket disconnected for user: {}, Total connections: {}", userId, sessionMap.size());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        log.info("Received WebSocket message from user {}: {}", userId, message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error: {}", error.getMessage());
    }

    public static void sendMessageToUser(String userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("Error sending WebSocket message to user {}: {}", userId, e.getMessage());
            }
        }
    }

    public static void broadcastMessage(String message) {
        for (Session session : sessionMap.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("Error broadcasting WebSocket message: {}", e.getMessage());
                }
            }
        }
    }
}
