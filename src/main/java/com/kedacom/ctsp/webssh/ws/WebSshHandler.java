package com.kedacom.ctsp.webssh.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.kedacom.ctsp.webssh.WebSSHUtil;
import com.kedacom.ctsp.webssh.hosts.HostLoginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.LongAdder;

@ServerEndpoint(value = "/ws/{id}")
@Component
public class WebSshHandler {

    static final Logger LOG = LoggerFactory.getLogger(WebSshHandler.class);

    public static LongAdder onlineCount = new LongAdder();

    public static LongAdder websocketSessionId = new LongAdder();

    public static Map<Long, HostLoginInfo> hostLoginInfoMap = new ConcurrentHashMap<>();

    private static CopyOnWriteArraySet<WebSshHandler> webSocketSet = new CopyOnWriteArraySet<>();

    private Session session;

    private static JSch jsch = new JSch();

    private com.jcraft.jsch.Session jschSession;

    private Channel channel;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Thread thread;

    @OnOpen
    public void onOpen(final Session session, @PathParam("id") Long id) throws JSchException, IOException, InterruptedException {
        this.session = session;
        LOG.info("session open, id=" + id + ", properties=" + session.getUserProperties());
        webSocketSet.add(this);
        onlineCount.increment();

        HostLoginInfo hostLoginInfo = hostLoginInfoMap.get(id);
        LOG.info("hostinfo binding, host=" + hostLoginInfo);

        jschSession = jsch.getSession(hostLoginInfo.getUsername(), hostLoginInfo.getHostname(), hostLoginInfo.getPort());
        jschSession.setPassword(hostLoginInfo.getPassword());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        jschSession.setConfig(config);
        jschSession.connect();
        channel = jschSession.openChannel("shell");
        inputStream = channel.getInputStream();
        outputStream = channel.getOutputStream();
        channel.connect();
        outputStream.write("touch /tmp/webssh.log\n".getBytes());
        outputStream.write("export HISTORY_FILE=/tmp/webssh.log\n".getBytes());
        outputStream.write("export PROMPT_COMMAND='{ date \"+%Y-%m-%d %T ##### USER:$USER IP:$SSH_CLIENT PS:$SSH_TTY ppid=$PPID pwd=$PWD  #### $(history 1 | { read x cmd; echo \"$cmd\"; })\";} >>$HISTORY_FILE'\n".getBytes());
        outputStream.flush();

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    int BUF_SIZE = 32 * 1024;
                    char[] chars = new char[BUF_SIZE];
                    int count = 0;
                    while ((count = bufferedReader.read(chars, 0, BUF_SIZE)) > 0) {
                        String msg = String.valueOf(chars, 0, count);
                        LOG.debug("terminal message received, line=" + msg);
                        byte[] bytes = msg.getBytes();
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
                        synchronized (this) {
                            session.getBasicRemote().sendBinary(byteBuffer);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("communication error", e);
                }
            }
        };
        thread.start();
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        onlineCount.decrement();
        LOG.info("session closed, session=" + this.session);
        if (channel != null) {
            channel.disconnect();
        }
        if (jschSession != null) {
            jschSession.disconnect();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        JsonNode node = WebSSHUtil.strToJsonObject(message);
        if (node.has("resize")) {
            int col = node.get("resize").get(0).asInt();
            int row = node.get("resize").get(1).asInt();
            LOG.info("resize command received , col=" + col + ", row=" + row);
            ChannelShell ch = (ChannelShell) channel;
            ch.setPtySize(col, row, col * 8, row * 8);
            return;
        }
        if (node.has("data")) {
            String command = node.get("data").asText();
            LOG.info("data command received, command=" + command);

            byte[] bytes = command.getBytes();
            outputStream.write(bytes);
            outputStream.flush();
            return;
        }
    }
}
