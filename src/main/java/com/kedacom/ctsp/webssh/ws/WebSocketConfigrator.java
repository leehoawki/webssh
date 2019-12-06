package com.kedacom.ctsp.webssh.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class WebSocketConfigrator extends ServerEndpointConfig.Configurator {

    static final Logger LOG = LoggerFactory.getLogger(WebSocketConfigrator.class);

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        if (httpSession == null) {
            return;
        }
        //把HttpSession中保存的ClientIP放到ServerEndpointConfig中，关键字可以跟之前不同
        config.getUserProperties().put("ClientIP", httpSession.getAttribute("ClientIP"));
    }
}
