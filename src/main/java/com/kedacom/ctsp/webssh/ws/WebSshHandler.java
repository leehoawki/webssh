package com.kedacom.ctsp.webssh.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.Channel;
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

    private StringBuilder dataToDst = new StringBuilder();

    private static JSch jsch = new JSch();

    private com.jcraft.jsch.Session jschSession;

    private Channel channel;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Thread thread;

    @OnOpen
    public void onOpen(final Session session, @PathParam("id") Long id) throws JSchException, IOException, EncodeException, InterruptedException {
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

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String msg = "";
                    String preMsg = "";
                    while ((msg = bufferedReader.readLine()) != null) {
                        LOG.debug("message received <<" + msg + ">>");
                        msg = "\r\n" + msg;
                        if (preMsg.equals(msg)) {
                            byte[] bytes = msg.getBytes();
                            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
                            synchronized (this) {
                                session.getBasicRemote().sendBinary(byteBuffer);
                            }
                            continue;
                        } else if (msg.equals(preMsg + dataToDst.toString())) {
                            continue;
                        }

                        if ("".equals(msg) || "\r\n".equals(msg)) {
                            continue;
                        }

                        preMsg = msg;
                        byte[] bytes = msg.getBytes();
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
                        synchronized (this) {
                            session.getBasicRemote().sendBinary(byteBuffer);
                        }

                        dataToDst = new StringBuilder();
                    }
                } catch (Exception e) {
                    LOG.error("", e);
                }
            }
        };
        thread.start();

        Thread.sleep(100);
        this.onMessage("{\"data\":\"\\r\"}", this.session);
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
    public void onMessage(String message, Session session) throws IOException, JSchException {
        LOG.debug("message received, message=" + message);
        JsonNode node = WebSSHUtil.strToJsonObject(message);

        if (node.has("resize")) {
            return;
        }

        if (node.has("data")) {
            String str = node.get("data").asText();
            if ("\r".equals(str)) {
                if (dataToDst.length() > 0) {
                    str = "\r\n";
                }
            } else {
                dataToDst.append(str);
            }

            byte[] bytes = str.getBytes();
            outputStream.write(bytes);
            outputStream.flush();

            if (!"\r\n".equals(str) && !"\r".equals(str)) {
                LOG.debug("data=" + str);
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
                synchronized (this) {
                    session.getBasicRemote().sendBinary(byteBuffer);
                }
            }
            LOG.debug("data sent, message=" + dataToDst.toString());
            return;
        }
    }
}
