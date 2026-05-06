package com.fast.agent.knowledge.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文档加载器
 */
@Slf4j
@Service
public class DocumentLoader {

    /**
     * 加载文本文件
     */
    public String loadTextFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String content = Files.readString(path);
        log.info("加载文本文件: filePath={}, length={}", filePath, content.length());
        return content;
    }

    /**
     * 加载 Markdown 文件
     */
    public String loadMarkdownFile(String filePath) throws IOException {
        return loadTextFile(filePath);
    }

    /**
     * 加载 PDF 文件
     */
    public String loadPdfFile(String filePath) {
        log.info("加载 PDF 文件: filePath={}", filePath);
        // TODO: 实现PDF解析逻辑
        return "";
    }

    /**
     * 加载 Word 文件
     */
    public String loadWordFile(String filePath) {
        log.info("加载 Word 文件: filePath={}", filePath);
        // TODO: 实现Word解析逻辑
        return "";
    }
}
