package org.scd.embedding;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.scd.embedding.param.QwenEmbeddingParam;
import org.scd.embedding.param.QwenEmbeddingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LocalQwen3EmbeddingModel implements EmbeddingModel {
    private HttpClient httpClient;

    private String embeddingUrl;

    private final Integer maxSegmentsPerBatch;

    public LocalQwen3EmbeddingModel(HttpClient httpClient, String embeddingUrl) {
        this.httpClient = httpClient;
        this.embeddingUrl = embeddingUrl;
        this.maxSegmentsPerBatch = 50;
    }

    public LocalQwen3EmbeddingModel(HttpClient httpClient, String embeddingUrl, Integer maxSegmentsPerBatch) {
        this.httpClient = httpClient;
        this.embeddingUrl = embeddingUrl;
        this.maxSegmentsPerBatch = maxSegmentsPerBatch;
    }

    @Override
    public Response<Embedding> embed(String text) {
        return embed(TextSegment.from(text));
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        QwenEmbeddingParam qwenEmbeddingParam = new QwenEmbeddingParam();
        qwenEmbeddingParam.setText(Collections.singletonList(textSegment.text()));
        QwenEmbeddingResult qwenEmbeddingResult = apiEmbedRequest(qwenEmbeddingParam);
        if (!qwenEmbeddingResult.embedSuccess()) {
            throw new RuntimeException("embedding error");
        }
        return new Response<>(new Embedding(qwenEmbeddingResult.getData().get(0)));
    }

    private QwenEmbeddingResult apiEmbedRequest(QwenEmbeddingParam qwenEmbeddingParam) {
        HttpRequest httpRequest = HttpRequest.builder()
                .url(embeddingUrl)
                .method(HttpMethod.POST)
                .addHeader("Content-Type", "application/json")
                .body(JSON.toJSONString(qwenEmbeddingParam))
                .build();
        SuccessfulHttpResponse successfulHttpResponse = httpClient.execute(httpRequest);
        return JSONObject.parseObject(successfulHttpResponse.body(), QwenEmbeddingResult.class);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<String> textList = new ArrayList<>();
        List<Embedding> resultList = new ArrayList<>();
        for (TextSegment textSegment : textSegments) {
            textList.add(textSegment.text());
            if (textList.size() == maxSegmentsPerBatch) {
                resultList.addAll(requestBatch(textList));
                textList.clear();
            }
        }
        if (!textList.isEmpty()) {
            resultList.addAll(requestBatch(textList));
        }
        return new Response<>(resultList);
    }


    private List<Embedding> requestBatch(List<String> textList) {
        QwenEmbeddingParam qwenEmbeddingParam = new QwenEmbeddingParam();
        qwenEmbeddingParam.setText(textList);
        QwenEmbeddingResult qwenEmbeddingResult = apiEmbedRequest(qwenEmbeddingParam);
        if (!qwenEmbeddingResult.embedSuccess()) {
            throw new RuntimeException("embedding error");
        }
        return qwenEmbeddingResult.getData().stream().map(Embedding::new).toList();
    }

    @Override
    public int dimension() {
        return 1024;
    }
}
