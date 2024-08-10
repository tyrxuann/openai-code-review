package dev.tyrxuan.middleware.sdk.domain.service.impl;

import com.google.common.collect.ImmutableList;
import dev.tyrxuan.middleware.sdk.domain.service.AbstractOpenAICodeReviewService;
import dev.tyrxuan.middleware.sdk.infrastructure.git.GitCommand;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.IOpenAI;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.request.ChatCompletionRequest;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.request.Model;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.response.ChatCompletionSyncResponse;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.WeiXin;
import dev.tyrxuan.middleware.sdk.infrastructure.weixin.model.TemplateMessage.TemplateKey;

import java.io.IOException;

public class OpenAICodeReviewService extends AbstractOpenAICodeReviewService {
    public OpenAICodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(ImmutableList.of(
                new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"),
                new ChatCompletionRequest.Prompt("user", diffCode)
        ));

        ChatCompletionSyncResponse completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponse.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String linkUrl) throws Exception {
        weiXin.templateMessage()
                .linkUrl(linkUrl)
                .putMessage(TemplateKey.REPO_NAME, gitCommand.getProject())
                .putMessage(TemplateKey.BRANCH_NAME, gitCommand.getBranch())
                .putMessage(TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor())
                .putMessage(TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage())
                .send();
    }
}
