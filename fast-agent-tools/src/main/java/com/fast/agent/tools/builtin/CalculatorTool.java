package com.fast.agent.tools.builtin;

import com.fast.agent.tools.base.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 计算器工具
 */
@Slf4j
@Component
public class CalculatorTool extends BaseTool {

    public CalculatorTool() {
        this.name = "calculator";
        this.description = "执行数学计算";
    }

    @Override
    public String execute(String input) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            Object result = engine.eval(input);
            log.info("计算器工具执行: input={}, result={}", input, result);
            return String.valueOf(result);
        } catch (ScriptException e) {
            log.error("计算器工具执行失败: input={}", input, e);
            return "计算错误: " + e.getMessage();
        }
    }
}
