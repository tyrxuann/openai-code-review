package dev.tyrxuan.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.response.ChatCompletionSyncResponse;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.model.TemplateMessage;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.utils.BearerTokenUtils;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.utils.WXAccessTokenUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ApiTest {
    public static void main(String[] args) {
        String apiKeySecret = "1c4e10cd5f5aa7db8f773de8d8e736cb.vUNEuveEPgMZXTJA";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {
        String apiKeySecret = "1c4e10cd5f5aa7db8f773de8d8e736cb.vUNEuveEPgMZXTJA";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String code = "1+1";

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言；请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";


        try(OutputStream os = connection.getOutputStream()){
            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }

    @Test
    public void test_wx_push() {
        String accessToken = WXAccessTokenUtils.getAccessToken("wx95176c705a75d99f", "3c708763d856036a7504e235934533e5");
        System.out.println(accessToken);

        TemplateMessage message = new TemplateMessage("oOEMv61IDYdE2Mys5jEXbK7ixCdg", "5KZkHzQMiiO5S8aMoHrzAYECaR4ChRwhqJH6dxF7kbg");
        message.setUrl("https://github.com/tyrxuann/openai-code-review-log/blob/master/2024-08-10/APQ1qLg59EE9.md");
        message.put(TemplateMessage.TemplateKey.REPO_NAME, "big-market");
        message.put(TemplateMessage.TemplateKey.COMMIT_MESSAGE, "feat: new feature");

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
