package org.scd;

import dev.langchain4j.community.store.embedding.duckdb.DuckDBEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import org.scd.parser.XmlParser;

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


    public static void main(String[] args) {

//        reload();

        // Search request
        var queryEmbedding = embeddingModel.embed("run class").content();
        var request = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).maxResults(15).build();

        var relevant = embeddingStore.search(request);
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.matches().get(0);

        // Show results
        System.out.println(embeddingMatch.score()); // 0.8416415629618381
        System.out.println(embeddingMatch.embedded().text()); //DuckDB is an amazing database engine!
        System.out.println(embeddingMatch.embedded().metadata());
    }

    private static void reload() {
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
