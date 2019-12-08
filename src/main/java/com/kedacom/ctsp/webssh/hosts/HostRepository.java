package com.kedacom.ctsp.webssh.hosts;

public interface HostRepository {
    HostLoginInfo getHost(String hostname);
}
