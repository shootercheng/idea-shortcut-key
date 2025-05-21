package org.scd;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.langchain4j.community.store.embedding.duckdb.DuckDBEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.scd.model.ChatModel;
import org.scd.parser.XmlParser;
import org.scd.translate.DataPersistence;
import org.scd.translate.KeyIdTranslate;
import org.scd.translate.TranslateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Hello world!
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    // Init Model and Store
    private static final DuckDBEmbeddingStore embeddingStore = DuckDBEmbeddingStore.builder()
            .filePath("database/shortcut-key.duck")
            .tableName("idea_plugin")
            .build();
    private static final AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel =
            new AllMiniLmL6V2QuantizedEmbeddingModel();


    public static void main(String[] args) {

        translate();

        // Search request
//        var queryEmbedding = embeddingModel.embed("run class").content();
//        var request = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).maxResults(15).build();
//
//        var relevant = embeddingStore.search(request);
//        EmbeddingMatch<TextSegment> embeddingMatch = relevant.matches().get(0);
//
//        // Show results
//        System.out.println(embeddingMatch.score()); // 0.8416415629618381
//        System.out.println(embeddingMatch.embedded().text()); //DuckDB is an amazing database engine!
//        System.out.println(embeddingMatch.embedded().metadata());
    }

    public static List<TranslateItem> translate() {
        XmlParser xmlParser = new XmlParser("C:\\Users\\Administrator\\Desktop\\idea-plugin\\keymap");
        xmlParser.parse();
        Map<String, List<String>> keyMapRes = xmlParser.getResult();
        StringBuilder stringBuilder = new StringBuilder();
        List<TranslateItem> translateItemList = new ArrayList<>();
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        keyMapRes.forEach((key, value) -> {
            TranslateItem translateItem = dataPersistence.queryDataByEn(key);
            if (translateItem.getEn() == null) {
                stringBuilder.append(key).append(",");
            }

        });
        if (!stringBuilder.isEmpty()) {
            addTranslate(stringBuilder, translateItemList);
        }
        dataPersistence.insertDataList(translateItemList);
        return translateItemList;
    }

    private static void addTranslate(StringBuilder stringBuilder, List<TranslateItem> translateItemList) {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties", true);
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .streamingChatLanguageModel(chatModel.getStreamingChatLanguageModel())
                .build();
        TokenStream tokenStream = keyIdTranslate.ideaKeyMapTranslateStream(stringBuilder.toString());
        AtomicReference<Boolean> isComplete = new AtomicReference<>(false);
        AtomicReference<String> res = new AtomicReference<>("");
        tokenStream.onPartialResponse((s) -> {
            LOGGER.info("partial response str {}", s);
        });
        tokenStream.onCompleteResponse(chatResponse -> {
            LOGGER.info("message text {}", chatResponse.aiMessage().text());
            isComplete.set(true);
            res.set(chatResponse.aiMessage().text());
        });
        tokenStream.onRetrieved(contents -> {
            LOGGER.info("content list {}", contents);
        });
        tokenStream.onError(throwable -> {
            LOGGER.error("request error ", throwable);
        });
        tokenStream.start();
        while (!isComplete.get()) {
            LOGGER.info("wait 10s..");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        var parseRes = JsonParser.parseString(res.get());
        if (parseRes instanceof JsonObject jsonObject) {
            Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonElement> data = iterator.next();
                TranslateItem translateItem = new TranslateItem(data.getKey(),
                        data.getValue().getAsString());
                translateItemList.add(translateItem);
            }
        }
    }

    private static void init() {
        embeddingStore.removeAll();
        XmlParser xmlParser = new XmlParser("C:\\Users\\Administrator\\Desktop\\idea-plugin\\keymap");
        xmlParser.parse();
        Map<String, List<String>> keyMapRes = xmlParser.getResult();
        keyMapRes.forEach((id, keyList) -> {
            Response<Embedding> embeddingResponse = embeddingModel.embed(id);
            Metadata metadata = new Metadata();
            int i = 1;
            keyList.forEach(key -> {
                metadata.put("keyboard-shortcut-" + i, key);
            });
            metadata.put("isDefault", String.valueOf(true));
            TextSegment textSegment = TextSegment.from(id, metadata);
            embeddingStore.add(embeddingResponse.content(), textSegment);
        });
    }
}
