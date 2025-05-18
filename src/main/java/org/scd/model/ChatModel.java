package org.scd.model;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ChatModel {

    private Properties modelProperties;

    private String configPath;

    private ChatLanguageModel chatLanguageModel;

    public ChatModel(String configPath) {
        this.configPath = configPath;
        modelProperties = new Properties();
        try {
            modelProperties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String baseUrl = modelProperties.getProperty("baseUrl");
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl未配置");
        }
        String apiKey = modelProperties.getProperty("apiKey");
        if (apiKey == null) {
            throw new IllegalArgumentException("apiKey未配置");
        }
        String modelName = modelProperties.getProperty("modelName");
        if (modelName == null) {
            throw new IllegalArgumentException("modelName未配置");
        }
        String logRequests = modelProperties.getProperty("logRequests");
        if (logRequests == null) {
            logRequests = "false";
        }
        String logResponses = modelProperties.getProperty("logResponses");
        if (logResponses == null) {
            logResponses = "false";
        }
        this.chatLanguageModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(Boolean.parseBoolean(logRequests))
                .logResponses(Boolean.parseBoolean(logResponses))
                .build();
    }

    public ChatLanguageModel getChatLanguageModel() {
        return chatLanguageModel;
    }
}
