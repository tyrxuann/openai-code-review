package dev.tyrxuan.middleware.sdk.infrastructure.weixin;

import com.alibaba.fastjson2.JSON;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.model.TemplateMessage;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.utils.WXAccessTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WeiXin {
    private static final Logger log = LoggerFactory.getLogger(WeiXin.class);

    private final String appId;

    private final String secret;

    private final String touser;

    private final String templateId;

    public WeiXin(String appId, String secret, String touser, String templateId) {
        this.appId = appId;
        this.secret = secret;
        this.touser = touser;
        this.templateId = templateId;
    }

    public TemplateMessageBuilder templateMessage() {
        return new TemplateMessageBuilder();
    }

    public class TemplateMessageBuilder {
        private final TemplateMessage message;

        public TemplateMessageBuilder() {
            message = new TemplateMessage(touser, templateId);
        }

        public TemplateMessageBuilder linkUrl(String linkUrl) {
            message.setUrl(linkUrl);
            return this;
        }

        public TemplateMessageBuilder putMessage(TemplateMessage.TemplateKey key, String value) {
            message.put(key, value);
            return this;
        }

        public void send() throws Exception {
            String accessToken = WXAccessTokenUtils.getAccessToken(appId, secret);

            URL url = new URL(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = JSON.toJSONString(message).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                log.info("send openai-code-review weixin template message: {}!", response);
            }
        }
    }
}
