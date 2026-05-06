package com.fast.agent.web;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Fast Agent 启动类
 */
@SpringBootApplication(scanBasePackages = "com.fast.agent")
public class FastAgentApplication {

    public static void main(String[] args) {
        // 加载 .env 文件中的环境变量，优先使用 .env 中的配置
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            // 直接设置系统属性，覆盖已有的环境变量
            System.setProperty(entry.getKey(), entry.getValue());
        });
        
        SpringApplication.run(FastAgentApplication.class, args);
        System.out.println("""

                ========================================
                   Fast Agent 启动成功！
                   访问地址: http://localhost:8080
                   接口文档: http://localhost:8080/doc.html
                ========================================
                """);
    }
}
