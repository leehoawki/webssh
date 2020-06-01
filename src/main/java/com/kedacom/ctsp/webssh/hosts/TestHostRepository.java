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


        HostLoginInfo hostLoginInfo2 = new HostLoginInfo();
        hostLoginInfo2.setHostname("10.9.9.76");
        hostLoginInfo2.setPort(22);
        hostLoginInfo2.setUsername("root");
        hostLoginInfo2.setPassword("kedacom");
        hosts.put("10.9.9.76", hostLoginInfo2);
    }

    @Override
    public HostLoginInfo getHost(String hostname) {
        return hosts.get(hostname);
    }
}
