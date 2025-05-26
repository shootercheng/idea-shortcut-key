package org.scd.model;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.scd.util.PropertiesUtil;

import java.time.Duration;
import java.util.Properties;

public class ChatModel {

    private Properties modelProperties;

    private String configPath;

    private OpenAiChatModel openAiChatModel;

    private StreamingChatModel streamingChatModel;

    public ChatModel(String configPath, Boolean isStream) {
        this.configPath = configPath;
        modelProperties = PropertiesUtil.loadByPath(configPath);
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
        if (isStream) {
            this.streamingChatModel = OpenAiStreamingChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .logRequests(Boolean.parseBoolean(logRequests))
                    .logResponses(Boolean.parseBoolean(logResponses))
                    .timeout(Duration.ofMinutes(3))
                    .build();
        } else {
            this.openAiChatModel = OpenAiChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .logRequests(Boolean.parseBoolean(logRequests))
                    .logResponses(Boolean.parseBoolean(logResponses))
                    .timeout(Duration.ofMinutes(3))
                    .build();
        }
    }

    public OpenAiChatModel getOpenAiChatModel() {
        return openAiChatModel;
    }

    public StreamingChatModel getStreamingChatModel() {
        return streamingChatModel;
    }
}
