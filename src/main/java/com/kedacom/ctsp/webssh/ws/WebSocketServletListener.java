package com.kedacom.ctsp.webssh.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@WebListener()
public class WebSocketServletListener implements ServletRequestListener {

    static final Logger LOG = LoggerFactory.getLogger(WebSocketServletListener.class);

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
        HttpSession session = request.getSession();
        LOG.info("client ip accquired, ip=" + servletRequestEvent.getServletRequest().getRemoteAddr());
        session.setAttribute("client_ip", servletRequestEvent.getServletRequest().getRemoteAddr());
    }
}
