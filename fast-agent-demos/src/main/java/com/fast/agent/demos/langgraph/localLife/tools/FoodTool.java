package com.fast.agent.demos.langgraph.localLife.tools;

import com.fast.agent.demos.langgraph.localLife.constants.LocalLifeConstants;
import org.springframework.stereotype.Component;

@Component
public class FoodTool implements LocalLifeTool {

    private static final String NAME = "FoodTool";

    @Override
    public String name() {
        return LocalLifeConstants.FOOD_TOOL_NAME;
    }

    @Override
    public String description() {
        return LocalLifeConstants.FOOD_TOOL_DESC;
    }

    @Override
    public String execute(String input) {
        return "上海最近的热榜美食是：西塔老太太、小杨生煎、shadows";
    }
}
