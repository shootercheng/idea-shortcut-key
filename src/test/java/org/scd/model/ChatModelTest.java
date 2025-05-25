package org.scd.model;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.junit.Assert;
import org.junit.Test;
import org.scd.translate.KeyIdTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ChatModelTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatModelTest.class);

    @Test
    public void testCreateChatModel() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties", false);
        Assert.assertNotNull(chatModel.getChatLanguageModel());
    }

    @Test
    public void testAiAskQuestion() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties", false);
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .chatLanguageModel(chatModel.getChatLanguageModel())
                .build();
        String aiAnswer = keyIdTranslate.answer("你好,你是谁");
        LOGGER.info("ai answer {}", aiAnswer);
        Assert.assertNotNull(aiAnswer);
    }

    @Test
    public void testAiTranslate() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties", false);
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .chatLanguageModel(chatModel.getChatLanguageModel())
                .build();
        String res = keyIdTranslate.ideaKeyMapTranslate("EditorToggleCase,GotoCustomRegion,FindInPath");
        LOGGER.info("translate res {}", res);
        Assert.assertNotNull(res);
    }

    @Test
    public void testStreamTranslate() throws ExecutionException, InterruptedException, TimeoutException {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties", true);
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .streamingChatLanguageModel(chatModel.getStreamingChatLanguageModel())
                .build();
        TokenStream tokenStream = keyIdTranslate.ideaKeyMapTranslateStream("EditorToggleCase,GotoCustomRegion,FindInPath");
        tokenStream.onPartialResponse((s) -> {
            LOGGER.info("partial response str {}", s);
        });
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();
        tokenStream.onCompleteResponse(chatResponse -> {
            LOGGER.info("message text {}", chatResponse.aiMessage().text());
            futureResponse.complete(chatResponse);
        });
        tokenStream.onRetrieved(contents -> {
            LOGGER.info("content list {}", contents);
        });
        tokenStream.onError(throwable -> {
            LOGGER.error("request error ", throwable);
        });
        tokenStream.start();
        ChatResponse chatResponse = futureResponse.get(3, TimeUnit.MINUTES);
        LOGGER.info("result text {}", chatResponse.aiMessage().text());
    }

}
