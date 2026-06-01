import com.fast.agent.core.langgraph.demo.localLife.LocalLifeGraph;
import com.fast.agent.core.langgraph.demo.queryWeather.WeatherAskGraph;
import com.fast.agent.core.langgraph.demo.simplest.SimpleGraphMain;
import com.fast.agent.core.langgraph.demo.travelPlan.TravelGraph;
import com.fast.agent.web.FastAgentApplication;
import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

@SpringBootTest(classes = FastAgentApplication.class)
@TestPropertySource(locations = "file:${user.dir}/../.env")
public class SimpleGraphTest {

    @Autowired
    private SimpleGraphMain simpleGraph;
    @Autowired
    private WeatherAskGraph weatherAskGraph;
    @Autowired
    private TravelGraph travelGraph;
    @Autowired
    private LocalLifeGraph localLifeGraph;

    @Test
    public void testExecute() throws GraphStateException {
        simpleGraph.execute("你好，介绍一下LangGraph4j");
    }

    @Test
    public void testWeatherGraph() throws GraphStateException {
        weatherAskGraph.execute("今天北京天气如何");
    }

    @Test
    public void testTravelGraph() throws GraphStateException {
        travelGraph.execute("我想去阿那亚看看");
    }

    @Test
    public void testLocalLifeGraph() throws GraphStateException {
        localLifeGraph.execute("附近遛狗的好去处");
    }
}
