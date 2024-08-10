package dev.tyrxuan.middleware.sdk.infrastructure.openai.impl;

import com.alibaba.fastjson2.JSON;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.IOpenAI;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.request.ChatCompletionRequest;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.response.ChatCompletionSyncResponse;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGLM implements IOpenAI {
    private final String apiHost;

    private final String apiKeySecret;

    public ChatGLM(String apiHost, String apiKeySecret) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKeySecret;
    }

    @Override
    public ChatCompletionSyncResponse completions(ChatCompletionRequest request) throws Exception {
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL(apiHost);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        try(OutputStream os = connection.getOutputStream()){
            byte[] input = JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        return JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
    }
}
