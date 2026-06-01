package com.fast.agent.core.langgraph.demo.localLife.tools;

import com.fast.agent.core.langgraph.demo.localLife.constants.LocalLifeConstants;
import org.springframework.stereotype.Component;

@Component
public class MallTool implements LocalLifeTool {
    @Override
    public String name() {
        return LocalLifeConstants.MALL_TOOL_NAME;
    }

    @Override
    public String description() {
        return LocalLifeConstants.FOOD_TOOL_DESC;
    }

    @Override
    public String execute(String input) {
        return "蟠龙天地、太古里、西岸梦中心";
    }
}
