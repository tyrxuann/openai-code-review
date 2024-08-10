package dev.tyrxuan.middleware.sdk.infrastructure.openai;

import dev.tyrxuan.middleware.sdk.infrastructure.openai.request.ChatCompletionRequest;
import dev.tyrxuan.middleware.sdk.infrastructure.openai.response.ChatCompletionSyncResponse;

public interface IOpenAI {
    ChatCompletionSyncResponse completions(ChatCompletionRequest request) throws Exception;
}
