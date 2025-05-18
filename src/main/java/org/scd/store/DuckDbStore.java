package org.scd.store;

import dev.langchain4j.community.store.embedding.duckdb.DuckDBEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

import static dev.langchain4j.internal.Utils.randomUUID;

public class DuckDbStore {
    private final DuckDBEmbeddingStore embeddingStore = DuckDBEmbeddingStore.builder()
            .filePath("database/shortcut-key.duck")
            .tableName("idea_plugin")
            .build();


    public String add(Embedding embedding, TextSegment textSegment) {
        return embeddingStore.add(embedding, textSegment);
    }

}
