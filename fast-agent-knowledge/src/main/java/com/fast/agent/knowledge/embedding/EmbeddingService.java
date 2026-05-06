package com.fast.agent.knowledge.embedding;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量化服务
 */
@Slf4j
@Service
public class EmbeddingService {

    @Value("${langchain4j.open-ai.api-key:}")
    private String apiKey;

    /**
     * 获取 EmbeddingModel 实例
     */
    public EmbeddingModel getEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-ada-002")
                .build();
    }

    /**
     * 文本向量化
     */
    public float[] embed(String text) {
        EmbeddingModel model = getEmbeddingModel();
        return model.embed(text).content().vector();
    }

    /**
     * 批量文本向量化
     */
    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingModel model = getEmbeddingModel();
        List<TextSegment> segments = texts.stream()
                .map(TextSegment::from)
                .toList();
        return model.embedAll(segments).content().stream()
                .map(embedding -> embedding.vector())
                .toList();
    }
}
