package com.fast.agent.core.skills;

import com.fast.agent.model.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
public class IntentBasedSkillPromptLoader {

    // 项目根目录 /prompts/skills
    private static final Path SKILL_DIR =
            Paths.get(System.getProperty("user.dir"))
                    .getParent()
                    .resolve("prompts/skills")
                    .toAbsolutePath();
    // 文件名 → 意图编码
    private static final Map<String, String> FILE_TO_INTENT = Map.of(
            "creative-clarification-skill", "creative-clarification",
            "market-analysis-skill",        "market-analysis",
            "product-definition-skill",    "product-definition",
            "creative-design-skill",       "creative-design",
            "default",                     "default"
    );

    // 缓存：意图编码 → 技能内容
    private final Map<String, String> skillCache = new HashMap<>();

    @PostConstruct
    public void loadSkills() {
        log.info("开始加载外部技能文件：{}", SKILL_DIR);

        try {
            if (!Files.exists(SKILL_DIR)) {
                Files.createDirectories(SKILL_DIR);
                log.info("创建技能目录：{}", SKILL_DIR);
            }

            try (Stream<Path> stream = Files.list(SKILL_DIR)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .forEach(this::loadFile);
            }

            ensureDefault();
            log.info("技能加载完成，共加载：{} 个", skillCache.size());
            log.info("已加载意图：{}", skillCache.keySet());

        } catch (Exception e) {
            log.error("技能加载失败", e);
        }
    }

    private void loadFile(Path path) {
        try {
            String fileName = path.getFileName().toString();
            String baseName = fileName.replace(".md", "");

            if (FILE_TO_INTENT.containsKey(baseName)) {
                String intentCode = FILE_TO_INTENT.get(baseName);
                String content = Files.readString(path, StandardCharsets.UTF_8);
                skillCache.put(intentCode, content);
                log.info("加载成功：{} → {}", fileName, intentCode);
            }
        } catch (Exception e) {
            log.error("加载文件失败：{}", path, e);
        }
    }

    private void ensureDefault() {
        skillCache.putIfAbsent("default", "你是智能助手。");
    }

    // 对外提供：根据意图获取技能
    public String getSkillPrompt(IntentType intentType) {
        String code = switch (intentType) {
            case CREATIVE_CLARIFICATION -> "creative-clarification";
            case MARKET_ANALYSIS -> "market-analysis";
            case PRODUCT_DEFINITION -> "product-definition";
            case CREATIVE_DESIGN -> "creative-design";
            default -> "default";
        };
        return skillCache.getOrDefault(code, skillCache.get("default"));
    }
}