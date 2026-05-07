package com.fast.agent.core.skills;

import com.fast.agent.model.enums.IntentType;
import com.fast.agent.model.enums.SessionStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 技能Prompt全局加载工具类
 * 项目启动时一次性加载所有技能文件到内存
 */
@Component
public class SkillsPromptLoader {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SkillsPromptLoader.class);

    // 外部技能文件目录路径（项目同级根目录）
    private static final String EXTERNAL_SKILLS_DIR = "prompts/skills";
    
    /**
     * 意图技能Prompt缓存：key=IntentType, value=技能Prompt内容
     */
    private final Map<String, String> skillsPromptCache = new HashMap<>();


    public SkillsPromptLoader() {
        // 无参构造函数
    }

    /**
     * 项目启动时从外部目录加载所有技能文件
     */
    @PostConstruct
    public void loadAllSkills() {
        try {
            // 构建外部技能文件目录路径
            Path skillsDir = Paths.get(EXTERNAL_SKILLS_DIR).toAbsolutePath();
            
            log.info("开始从外部目录加载技能Prompt文件：{}", skillsDir);
            
            // 检查目录是否存在
            if (!Files.exists(skillsDir)) {
                log.warn("外部技能文件目录不存在：{}，将创建目录并使用默认技能", skillsDir);
                Files.createDirectories(skillsDir);
                createDefaultSkillsFile(skillsDir);
                return;
            }
            
            // 扫描目录下的所有 .md 文件
            try (Stream<Path> paths = Files.list(skillsDir)) {
                long mdFileCount = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".md"))
                    .count();
                
                log.info("找到{}个技能文件", mdFileCount);
                
                // 重新打开流来处理文件
                try (Stream<Path> filePaths = Files.list(skillsDir)) {
                    filePaths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".md"))
                        .forEach(this::loadSkillFileFromPath);
                }
            }
            
            // 确保默认技能存在
            ensureDefaultSkill();

            log.info("意图技能Prompt加载完成，共加载{}个技能", skillsPromptCache.size());
            log.info("已加载的意图技能：{}", skillsPromptCache.keySet());

        } catch (IOException e) {
            log.error("加载技能Prompt文件失败", e);
            throw new RuntimeException("技能Prompt加载失败", e);
        }
    }

    /**
     * 从外部路径加载单个技能文件
     * @param filePath 文件路径
     */
    private void loadSkillFileFromPath(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            if (!filename.toLowerCase().endsWith(".md")) {
                return;
            }
            
            // 从文件名提取意图编码（去掉.md后缀）
            String intentCode = filename.substring(0, filename.length() - 3).toLowerCase();
            IntentType intentType = IntentType.fromCode(intentCode);
            
            // 读取文件内容
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            
            // 存入缓存
            skillsPromptCache.put(intentType.getCode(), content);
            
            log.debug("加载技能文件成功：{} -> {}", filename, intentType);
            
        } catch (Exception e) {
            log.error("加载技能文件失败：{}", filePath.getFileName(), e);
        }
    }


    /**
     * 创建默认技能文件
     * @param skillsDir 技能文件目录
     */
    private void createDefaultSkillsFile(Path skillsDir) {
        try {
            Path defaultFile = skillsDir.resolve("default.md");
            String defaultContent = """
                # 默认技能
                
                你是一个智能助手，能够回答各种问题并提供有用的建议。
                请根据用户的问题，提供准确、有帮助的回答。
                如果不确定答案，请诚实地说明。
                """;
            
            Files.writeString(defaultFile, defaultContent, StandardCharsets.UTF_8);
            log.info("已创建默认技能文件：{}", defaultFile);
            
            // 加载默认技能
            skillsPromptCache.put(IntentType.DEFAULT.getCode(), defaultContent);
            
        } catch (IOException e) {
            log.error("创建默认技能文件失败", e);
        }
    }

    
    /**
     * 确保默认技能存在
     */
    private void ensureDefaultSkill() {
        if (!skillsPromptCache.containsKey(IntentType.DEFAULT.getCode())) {
            String defaultPrompt = """
                你是一个智能助手，能够回答各种问题并提供有用的建议。
                请根据用户的问题，提供准确、有帮助的回答。
                如果不确定答案，请诚实地说明。
                """;
            skillsPromptCache.put(IntentType.DEFAULT.getCode(), defaultPrompt);
            log.warn("未找到default.md，使用内置默认Prompt");
        }
    }


    /**
     * 创建默认的流程步骤技能Prompt
     * @param step 流程步骤
     * @return 默认Prompt
     */
    private String createDefaultStepPrompt(SessionStep step) {
        return switch (step) {
            case CREATIVE_CLARIFY -> """
                你是一个产品创意澄清专家，负责帮助用户理清和细化产品创意。
                
                你的职责：
                1. 深入了解用户的创意想法
                2. 帮助用户明确产品的核心价值
                3. 引导用户完善创意细节
                4. 识别创意中的关键要素和潜在问题
                
                请通过提问和讨论，帮助用户完善产品创意。
                当创意澄清完成后，请输出：**阶段完成**
                """;
                
            case MARKET_ANALYSIS -> """
                你是一个市场分析专家，负责分析产品创意的市场可行性。
                
                你的职责：
                1. 分析目标市场规模和潜力
                2. 评估竞争环境和市场机会
                3. 识别目标用户群体和需求
                4. 分析市场趋势和风险因素
                
                请提供全面的市场分析报告。
                当市场分析完成后，请输出：**阶段完成**
                """;
                
            case PRODUCT_DEFINITION -> """
                你是一个产品定义专家，负责将创意转化为具体的产品规格。
                
                你的职责：
                1. 明确产品的核心功能和特性
                2. 定义产品的技术规格和参数
                3. 确定产品的用户体验设计
                4. 制定产品的开发优先级
                
                请提供详细的产品定义文档。
                当产品定义完成后，请输出：**阶段完成**
                """;
                
            case DESIGN_SELECTION -> """
                你是一个设计选择专家，负责为产品提供设计方案和选择建议。
                
                你的职责：
                1. 提供多种设计方案选项
                2. 分析各方案的优缺点
                3. 考虑成本、技术可行性等因素
                4. 给出最终的设计选择建议
                
                请提供完整的设计选择分析。
                当设计选择完成后，请输出：**阶段完成**
                """;
                
            default -> "请继续我们的对话。";
        };
    }

    /**
     * 根据意图类型获取对应的技能Prompt
     * @param intentType 意图类型
     * @return 技能Prompt内容，无匹配时返回默认Prompt
     */
    public String getSkillPrompt(IntentType intentType) {
        String prompt = skillsPromptCache.get(intentType.getCode());
        if (prompt == null) {
            log.warn("未找到意图{}对应的技能Prompt，使用默认Prompt", intentType);
            prompt = skillsPromptCache.get(IntentType.DEFAULT.getCode());
        }
        return prompt;
    }

    /**
     * 根据意图编码获取对应的技能Prompt
     * @param intentCode 意图编码
     * @return 技能Prompt内容，无匹配时返回默认Prompt
     */
    public String getSkillPrompt(String intentCode) {
        IntentType intentType = IntentType.fromCode(intentCode);
        return getSkillPrompt(intentType);
    }
}
