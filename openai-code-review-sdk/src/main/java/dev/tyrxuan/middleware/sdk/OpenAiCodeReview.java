package dev.tyrxuan.middleware.sdk;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableList;
import dev.tyrxuan.middleware.sdk.domain.model.ChatCompletionRequest;
import dev.tyrxuan.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import dev.tyrxuan.middleware.sdk.domain.model.Message;
import dev.tyrxuan.middleware.sdk.domain.model.Model;
import dev.tyrxuan.middleware.sdk.types.utils.BearerTokenUtils;
import dev.tyrxuan.middleware.sdk.types.utils.WXAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("openai 代码评审，测试执行");

        String token = System.getenv("GITHUB_TOKEN");
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }

        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code: " + exitCode);

        System.out.println("diff code: " + diffCode);

        // 2. chatglm 代码评审
        String log = codeReview(diffCode.toString());
        System.out.println("code review: " + log);

        // 3. 写入评审日志
        String logUrl = writeLog(token, log);
        System.out.println("writeLog: " + logUrl);

        // 4. 消息通知
        System.out.println("pushMessage: " + logUrl);
        pushMessage(logUrl);
    }

    private static String codeReview(String diffCode) throws Exception {
        String apiKeySecret = "1c4e10cd5f5aa7db8f773de8d8e736cb.vUNEuveEPgMZXTJA";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(ImmutableList.of(
                new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"),
                new ChatCompletionRequest.Prompt("user", diffCode)
        ));

        try(OutputStream os = connection.getOutputStream()){
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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
        return response.getChoices().get(0).getMessage().getContent();
    }

    private static String writeLog(String token, String log) throws Exception {
        Git git = Git.cloneRepository()
                .setURI("https://github.com/tyrxuann/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String filename = generateRandomString(12) + ".md";
        File logFile = new File(dateFolder, filename);
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(log);
        }

        git.add().addFilepattern(dateFolderName + "/" + filename).call();
        git.commit().setMessage("Add new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        System.out.println("Changes have been pushed to the repository.");

        return "https://github.com/tyrxuann/openai-code-review-log/blob/master/" + dateFolderName + "/" + filename;
    }

    private static void pushMessage(String logUrl) {
        String accessToken = WXAccessTokenUtils.getAccessToken();

        Message message = new Message();
        message.setUrl(logUrl);
        message.put("project", "big-market");
        message.put("review", logUrl);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
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
