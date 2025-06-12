package org.scd.embedding.param;

import lombok.Data;

import java.util.List;

@Data
public class QwenEmbeddingParam {
    private List<String> text;
}
