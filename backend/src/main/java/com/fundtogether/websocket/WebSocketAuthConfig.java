package com.fundtogether.websocket;

import com.fundtogether.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket认证配置类
 * <p>
 * 继承自Jakarta WebSocket的ServerEndpointConfig.Configurator，
 * 在WebSocket握手阶段对客户端进行JWT身份认证。
 * <p>
 * 支持两种Token传递方式：
 * <ol>
 *   <li>通过WebSocket子协议头（sec-websocket-protocol）传递</li>
 *   <li>通过URL查询参数（token=xxx）传递</li>
 * </ol>
 * 认证成功后，将用户ID和认证状态存入ServerEndpointConfig的UserProperties中，
 * 供WebSocketServer端点在连接建立时进行二次校验。
 * </p>
 */
@Slf4j
@Component
public class WebSocketAuthConfig extends ServerEndpointConfig.Configurator {

    /**
     * 修改WebSocket握手过程，在握手阶段进行JWT认证
     * <p>
     * 认证流程：
     * 1. 优先从sec-websocket-protocol请求头中获取Token；
     * 2. 若请求头中无Token，则从URL查询参数token中获取；
     * 3. 解析Token验证合法性，提取用户ID；
     * 4. 将认证结果和用户ID存入UserProperties，供后续WebSocket端点使用。
     * </p>
     *
     * @param sec      WebSocket服务端点配置对象
     * @param request  WebSocket握手请求对象
     * @param response WebSocket握手响应对象
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        String token = null;

        // 优先从WebSocket子协议头中获取Token（适用于前端通过new WebSocket(url, [token])方式连接）
        if (request.getHeaders() != null && request.getHeaders().get("sec-websocket-protocol") != null) {
            java.util.List<String> protocols = request.getHeaders().get("sec-websocket-protocol");
            if (!protocols.isEmpty()) {
                token = protocols.get(0);
            }
        }

        // 若子协议头中无Token，则从URL查询参数中获取（适用于WebSocket(url?token=xxx)方式连接）
        if (token == null && request.getParameterMap() != null) {
            java.util.List<String> tokenParams = request.getParameterMap().get("token");
            if (tokenParams != null && !tokenParams.isEmpty()) {
                token = tokenParams.get(0);
            }
        }

        // 解析Token并存储认证信息
        if (token != null && !token.isEmpty()) {
            try {
                // 解析JWT令牌，验证签名和有效期
                Claims claims = JwtUtils.parseToken(token);
                // 从令牌中提取用户ID
                Long userId = claims.get("userId", Long.class);
                // 将用户ID存入UserProperties，供WebSocket端点获取
                sec.getUserProperties().put("userId", userId);
                // 标记认证成功
                sec.getUserProperties().put("authenticated", true);
            } catch (Exception e) {
                // JWT验证失败，记录警告日志
                log.warn("WebSocket JWT验证失败: {}", e.getMessage());
                sec.getUserProperties().put("authenticated", false);
            }
        } else {
            // 未提供Token，标记为未认证
            sec.getUserProperties().put("authenticated", false);
        }
    }
}
