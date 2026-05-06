package com.fast.agent.knowledge.splitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分割器
 */
@Slf4j
@Service
public class TextSplitter {

    /**
     * 默认分割大小
     */
    private static final int DEFAULT_CHUNK_SIZE = 500;

    /**
     * 默认重叠大小
     */
    private static final int DEFAULT_OVERLAP_SIZE = 50;

    /**
     * 分割文本
     */
    public List<String> split(String text) {
        return split(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 分割文本（自定义参数）
     */
    public List<String> split(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            start = end - overlapSize;
        }

        log.info("文本分割: originalLength={}, chunkCount={}", length, chunks.size());
        return chunks;
    }
}
