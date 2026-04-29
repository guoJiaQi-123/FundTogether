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

/**
 * WebSocket服务端端点
 * <p>
 * 提供基于WebSocket的实时双向通信能力，支持按用户ID进行点对点消息推送和全局广播。
 * 使用ConcurrentHashMap维护所有在线用户的会话映射，确保线程安全。
 * <p>
 * 连接地址格式：ws://host/ws/{userId}，其中userId为路径参数。
 * 认证方式：通过WebSocketAuthConfig在握手阶段进行JWT认证，
 * 连接建立时还会校验路径中的userId与Token中的userId是否一致，防止身份伪造。
 * </p>
 */
@ServerEndpoint(value = "/ws/{userId}", configurator = WebSocketAuthConfig.class)
@Component
@Slf4j
public class WebSocketServer {

    /** 在线用户会话映射表，key为用户ID字符串，value为对应的WebSocket会话 */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * WebSocket连接建立时的回调方法
     * <p>
     * 执行双重校验：
     * 1. 检查用户是否已通过JWT认证（由WebSocketAuthConfig在握手阶段完成）；
     * 2. 校验URL路径中的userId与JWT令牌中的userId是否一致，防止身份伪造。
     * 校验通过后，将用户会话加入在线映射表。
     * </p>
     *
     * @param session WebSocket会话对象
     * @param userId  路径参数中的用户ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // 第一重校验：检查JWT认证是否通过
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

        // 第二重校验：校验路径中的userId与Token中的userId是否一致
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

        // 校验通过，将会话加入在线映射表
        sessionMap.put(userId, session);
        log.info("WebSocket connected for user: {}, Total connections: {}", userId, sessionMap.size());
    }

    /**
     * WebSocket连接关闭时的回调方法
     * <p>
     * 将断开连接的用户会话从在线映射表中移除。
     * </p>
     *
     * @param userId 路径参数中的用户ID
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        // 从在线映射表中移除该用户的会话
        sessionMap.remove(userId);
        log.info("WebSocket disconnected for user: {}, Total connections: {}", userId, sessionMap.size());
    }

    /**
     * 接收到WebSocket消息时的回调方法
     * <p>
     * 当前仅记录收到的消息日志，可根据业务需求扩展消息处理逻辑。
     * </p>
     *
     * @param message 客户端发送的消息内容
     * @param session WebSocket会话对象
     * @param userId  路径参数中的用户ID
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        log.info("Received WebSocket message from user {}: {}", userId, message);
    }

    /**
     * WebSocket发生错误时的回调方法
     *
     * @param session 发生错误的WebSocket会话对象
     * @param error   错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error: {}", error.getMessage());
    }

    /**
     * 向指定用户发送消息（点对点推送）
     * <p>
     * 根据用户ID查找其在线会话，若会话存在且处于打开状态，则发送文本消息。
     * 若用户不在线或会话已关闭，则不执行任何操作。
     * </p>
     *
     * @param userId  目标用户ID
     * @param message 要发送的消息内容
     */
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

    /**
     * 广播消息给所有在线用户
     * <p>
     * 遍历在线映射表中所有处于打开状态的会话，向每个会话发送相同的文本消息。
     * </p>
     *
     * @param message 要广播的消息内容
     */
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
