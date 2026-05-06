package com.fast.agent.knowledge.retrieve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 相似度检索服务
 */
@Slf4j
@Service
public class RetrievalService {

    /**
     * 检索相关文档
     */
    public List<DocumentChunk> retrieve(String query, int topK) {
        log.info("检索相关文档: query={}, topK={}", query, topK);
        // TODO: 实现向量检索逻辑
        return new ArrayList<>();
    }

    /**
     * 计算余弦相似度
     */
    public double cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量长度不一致");
        }

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 文档块类
     */
    public static class DocumentChunk {
        private String content;
        private double score;

        public DocumentChunk(String content, double score) {
            this.content = content;
            this.score = score;
        }

        public String getContent() {
            return content;
        }

        public double getScore() {
            return score;
        }
    }
}
