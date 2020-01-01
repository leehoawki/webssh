package com.kedacom.ctsp.webssh.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kedacom.ctsp.webssh.WebSSHUtil;
import com.kedacom.ctsp.webssh.hosts.HostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.kedacom.ctsp.webssh.hosts.HostLoginInfo;
import com.kedacom.ctsp.webssh.ws.WebSshHandler;

@Controller
public class WebSshController {

    static final Logger LOG = LoggerFactory.getLogger(WebSshController.class);

    @Autowired
    HostRepository repository;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ObjectNode connect(String hostname) {
        LOG.info("connecting..., hostname=" + hostname);
        WebSshHandler.websocketSessionId.increment();
        long wsId = WebSshHandler.websocketSessionId.longValue();

        HostLoginInfo hostLoginInfo = repository.getHost(hostname);
        WebSshHandler.hostLoginInfoMap.put(wsId, hostLoginInfo);

        ObjectNode node = WebSSHUtil.createObjectNode();
        node.put("status", 0);
        node.put("id", wsId);
        node.put("encoding", "utf-8");
        return node;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(String id) {
        return "index.html";
    }
}
