package com.kedacom.ctsp.webssh.hosts;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TestHostRepository implements HostRepository {

    static Map<String, HostLoginInfo> hosts = new HashMap<>();

    static {
        HostLoginInfo hostLoginInfo = new HostLoginInfo();
        hostLoginInfo.setHostname("192.168.1.109");
        hostLoginInfo.setPort(22);
        hostLoginInfo.setUsername("root");
        hostLoginInfo.setPassword("toor");
        hosts.put("192.168.1.109", hostLoginInfo);
    }

    @Override
    public HostLoginInfo getHost(String hostname) {
        return hosts.get(hostname);
    }
}
