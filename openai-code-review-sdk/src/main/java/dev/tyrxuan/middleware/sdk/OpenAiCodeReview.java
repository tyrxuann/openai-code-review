package dev.tyrxuan.middleware.sdk;

import dev.tyrxuan.middleware.sdk.domain.service.impl.OpenAICodeReviewService;
import dev.tyrxuan.middleware.sdk.infrastructure.git.GitCommand;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.IOpenAI;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.impl.ChatGLM;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAiCodeReview {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCodeReview.class);

    // 微信配置
    private static final String WEIXIN_APPID = "WEIXIN_APPID";
    private static final String WEIXIN_SECRET = "WEIXIN_SECRET";
    private static final String WEIXIN_TOUSER = "WEIXIN_TOUSER";
    private static final String WEIXIN_TEMPLATE_ID = "WEIXIN_TEMPLATE_ID";

    // ChatGLM 配置
    private static final String CHATGLM_API_HOST = "CHATGLM_API_HOST";
    private static final String CHATGLM_API_KEY_SECRET = "CHATGLM_API_KEY_SECRET";

    // Github 配置
    private static final String GITHUB_REVIEW_LOG_URI = "GITHUB_REVIEW_LOG_URI";
    private static final String GITHUB_TOKEN = "GITHUB_TOKEN";

    // 工程配置 - 自动获取
    private static final String COMMIT_PROJECT = "COMMIT_PROJECT";
    private static final String COMMIT_BRANCH = "COMMIT_BRANCH";
    private static final String COMMIT_AUTHOR = "COMMIT_AUTHOR";
    private static final String COMMIT_MESSAGE = "COMMIT_MESSAGE";

    public static void main(String[] args) {
        GitCommand gitCommand = new GitCommand(
                getEnv(GITHUB_REVIEW_LOG_URI),
                getEnv(GITHUB_TOKEN),
                getEnv(COMMIT_PROJECT),
                getEnv(COMMIT_BRANCH),
                getEnv(COMMIT_AUTHOR),
                getEnv(COMMIT_MESSAGE)
        );

        WeiXin weiXin = new WeiXin(
                getEnv(WEIXIN_APPID),
                getEnv(WEIXIN_SECRET),
                getEnv(WEIXIN_TOUSER),
                getEnv(WEIXIN_TEMPLATE_ID)
        );

        IOpenAI openAI = new ChatGLM(getEnv(CHATGLM_API_HOST), getEnv(CHATGLM_API_KEY_SECRET));

        OpenAICodeReviewService openAICodeReviewService = new OpenAICodeReviewService(gitCommand, openAI, weiXin);
        openAICodeReviewService.exec();

        log.info("openai-code-review done!");
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException(key + " is null");
        }
        return value;
    }
}
