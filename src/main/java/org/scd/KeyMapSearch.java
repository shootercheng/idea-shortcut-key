package org.scd;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

public class KeyMapSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyMapSearch.class);

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                LOGGER.info("User: ");
                String userQuery = scanner.nextLine();
                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }
                // Search request
                var queryEmbedding = InitStore.embeddingModel.embed(userQuery).content();
                var request = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .minScore(0.8D)
                        .maxResults(20).build();
                var relevant =  InitStore.embeddingStore.search(request);
                List<EmbeddingMatch<TextSegment>> embeddingMatchList = relevant.matches();
                LOGGER.info("=====================Result==========================");
                if (!embeddingMatchList.isEmpty()) {
                    embeddingMatchList.forEach(embeddingMatch -> {
                        LOGGER.info("score:{}, text:{}, metadata {}",
                                embeddingMatch.score(), embeddingMatch.embedded().text(),
                                embeddingMatch.embedded().metadata());
                    });
                }
            }
        }

    }
}
