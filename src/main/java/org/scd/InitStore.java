package org.scd;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.community.store.embedding.duckdb.DuckDBEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.scd.model.ChatModel;
import org.scd.parser.XmlParser;
import org.scd.translate.DataPersistence;
import org.scd.translate.KeyIdTranslate;
import org.scd.translate.TranslateItem;
import org.scd.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class InitStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitStore.class);

    private static final String DUCKDB_PATH = "database/shortcut-key.duck";

    private static final String MODEL_CONFIG_PATH = "/config/shortcut-key/model.properties";

    private static final String KEYMAP_PATH = "keymap";

    private static final String VECTOR_TABLE_NAME = "idea_plugin_openai";

    // Init Model and Store
    public static final DuckDBEmbeddingStore embeddingStore = DuckDBEmbeddingStore.builder()
            .filePath(DUCKDB_PATH)
            .tableName(VECTOR_TABLE_NAME)
            .build();

    private static Properties properties;

    static {
        properties  = PropertiesUtil.loadByPath(MODEL_CONFIG_PATH);
    }


    public static final EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
            .baseUrl(properties.getProperty("baseUrl"))
            .apiKey(properties.getProperty("apiKey"))
            .modelName("text-embedding-v3")
            .logRequests(true)
            .logResponses(true)
            .build();


    public static void main(String[] args) {
        Map<String, List<String>> keyMapRes = parseXml();
        List<TranslateItem> translateItemList = translate(keyMapRes);
        // save to idea_plugin table
        // reset
//        embeddingStore.removeAll();
        init(keyMapRes, translateItemList);
    }

    private static Map<String, List<String>> parseXml() {
        XmlParser xmlParser = new XmlParser(KEYMAP_PATH);
        xmlParser.parse();
        Map<String, List<String>> keyMapRes = xmlParser.getResult();
        LOGGER.info("xml parse key size {}", keyMapRes.size());
        return keyMapRes;
    }

    public static List<TranslateItem> translate(Map<String, List<String>> keyMapRes) {
        StringBuilder stringBuilder = new StringBuilder();
        List<TranslateItem> translateItemList = new ArrayList<>();
        DataPersistence dataPersistence = new DataPersistence(DUCKDB_PATH);
        keyMapRes.forEach((key, value) -> {
            TranslateItem translateItem = dataPersistence.queryDataByEn(key);
            if (translateItem.getEn() == null) {
                stringBuilder.append(key).append(",");
            }
        });
        if (!stringBuilder.isEmpty()) {
            addTranslate(stringBuilder, translateItemList);
        }
        if (!translateItemList.isEmpty()) {
            dataPersistence.insertDataList(translateItemList);
        }
        LOGGER.info("translate en to cn size {}", translateItemList.size());
        // query db en to cn data
        List<TranslateItem> dbItemList = dataPersistence.queryAllData();
        LOGGER.info("db exists translate en to cn size {}", translateItemList.size());
        return dbItemList;
    }

    private static void addTranslate(StringBuilder stringBuilder, List<TranslateItem> translateItemList) {
        ChatModel chatModel = new ChatModel(MODEL_CONFIG_PATH, true);
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .streamingChatModel(chatModel.getStreamingChatModel())
                .build();
        TokenStream tokenStream = keyIdTranslate.ideaKeyMapTranslateStream(stringBuilder.toString());
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
        String res = null;
        try {
            ChatResponse chatResponse = futureResponse.get(3, TimeUnit.MINUTES);
            res = chatResponse.aiMessage().text();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (res != null) {
            JSONObject jsonObject = JSONObject.parseObject(res);
            for (Map.Entry<String, Object> data : jsonObject.entrySet()) {
                TranslateItem translateItem = new TranslateItem(data.getKey(),
                        String.valueOf(data.getValue()));
                translateItemList.add(translateItem);
            }
        }
    }

    private static void init(Map<String, List<String>> keyMapRes, List<TranslateItem> translateItemList) {
        // add origin
        keyMapRes.forEach((key, value) -> {
            Metadata metadata = new Metadata();
            metadata.put("id", key);
            add(key, value, metadata);
        });
        translateItemList.forEach(translateItem -> {
            List<String> value = keyMapRes.get(translateItem.getEn());
            if (value != null && !value.isEmpty()) {
                Metadata metadata = new Metadata();
                metadata.put("id", translateItem.getCn());
                metadata.put("originId", translateItem.getEn());
                add(translateItem.getCn(), value, metadata);
            }
        });
    }

    private static void add(String id, List<String> keyList, Metadata metadata) {
        var queryEmbedding = embeddingModel.embed(id).content();
        var request = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding)
                .minScore(0.99D)
                .maxResults(1)
                .filter(new IsEqualTo("id", id)).build();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
        if (matches.isEmpty()) {
            AtomicInteger i = new AtomicInteger(1);
            keyList.forEach(key -> {
                metadata.put("keyboard-shortcut-" + i.get(), key);
                i.getAndIncrement();
            });
            metadata.put("id", id);
            TextSegment textSegment = TextSegment.from(id, metadata);
            embeddingStore.add(queryEmbedding, textSegment);
        }
    }


}
