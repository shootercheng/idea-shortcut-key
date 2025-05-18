package org.scd.model;

import dev.langchain4j.service.AiServices;
import org.junit.Assert;
import org.junit.Test;
import org.scd.translate.KeyIdTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatModelTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatModelTest.class);

    @Test
    public void testCreateChatModel() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties");
        Assert.assertNotNull(chatModel.getChatLanguageModel());
    }

    @Test
    public void testAiAskQuestion() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties");
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .chatLanguageModel(chatModel.getChatLanguageModel())
                .build();
        String aiAnswer = keyIdTranslate.answer("你好,你是谁");
        LOGGER.info("ai answer {}", aiAnswer);
        Assert.assertNotNull(aiAnswer);
    }

    @Test
    public void testAiTranslate() {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties");
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .chatLanguageModel(chatModel.getChatLanguageModel())
                .build();
        String res = keyIdTranslate.ideaKeyMapTranslate("EditorToggleCase,GotoCustomRegion,FindInPath");
        LOGGER.info("translate res {}", res);
        Assert.assertNotNull(res);
    }

}
