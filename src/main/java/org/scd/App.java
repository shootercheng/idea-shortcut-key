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
import org.scd.model.ChatModel;
import org.scd.parser.XmlParser;
import org.scd.translate.DataPersistence;
import org.scd.translate.KeyIdTranslate;
import org.scd.translate.TranslateItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {

    // Init Model and Store
    private static final DuckDBEmbeddingStore embeddingStore = DuckDBEmbeddingStore.builder()
            .filePath("database/shortcut-key.duck")
            .tableName("idea_plugin")
            .build();
    private static final AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel =
            new AllMiniLmL6V2QuantizedEmbeddingModel();

    private static int BATCH_SIZE = 50;


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
        final int[] count = {0};
        keyMapRes.forEach((key, value) -> {
            if (count[0] == BATCH_SIZE) {
                addTranslate(stringBuilder, translateItemList);
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(key).append(",");
            }
            count[0]++;
        });
        if (count[0] > 0) {
            addTranslate(stringBuilder, translateItemList);
        }
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        dataPersistence.insertDataList(translateItemList);
        return translateItemList;
    }

    private static void addTranslate(StringBuilder stringBuilder, List<TranslateItem> translateItemList) {
        ChatModel chatModel = new ChatModel("/config/shortcut-key/model.properties");
        KeyIdTranslate keyIdTranslate = AiServices.builder(KeyIdTranslate.class)
                .chatLanguageModel(chatModel.getChatLanguageModel())
                .build();
        String res = keyIdTranslate.ideaKeyMapTranslate(stringBuilder.toString());
        var parseRes = JsonParser.parseString(res);
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
