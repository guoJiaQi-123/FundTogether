package com.fundtogether.websocket;

import com.fundtogether.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketAuthConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        String token = null;

        if (request.getHeaders() != null && request.getHeaders().get("sec-websocket-protocol") != null) {
            java.util.List<String> protocols = request.getHeaders().get("sec-websocket-protocol");
            if (!protocols.isEmpty()) {
                token = protocols.get(0);
            }
        }

        if (token == null && request.getParameterMap() != null) {
            java.util.List<String> tokenParams = request.getParameterMap().get("token");
            if (tokenParams != null && !tokenParams.isEmpty()) {
                token = tokenParams.get(0);
            }
        }

        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = JwtUtils.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                sec.getUserProperties().put("userId", userId);
                sec.getUserProperties().put("authenticated", true);
            } catch (Exception e) {
                log.warn("WebSocket JWT验证失败: {}", e.getMessage());
                sec.getUserProperties().put("authenticated", false);
            }
        } else {
            sec.getUserProperties().put("authenticated", false);
        }
    }
}
