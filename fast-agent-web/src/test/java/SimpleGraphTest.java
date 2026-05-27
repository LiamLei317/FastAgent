import com.fast.agent.core.langgraph.demo.queryWeather.WeatherAskGraph;
import com.fast.agent.core.langgraph.demo.simplest.SimpleGraphMain;
import com.fast.agent.web.FastAgentApplication;
import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = FastAgentApplication.class)
@TestPropertySource(locations = "file:${user.dir}/../.env")
public class SimpleGraphTest {

    @Autowired
    private SimpleGraphMain simpleGraph;
    @Autowired
    private WeatherAskGraph weatherAskGraph;

    @Test
    public void testExecute() throws GraphStateException {
        simpleGraph.execute("你好，介绍一下LangGraph4j");
    }

    @Test
    public void testWeatherGraph() throws GraphStateException {
        weatherAskGraph.execute("今天北京天气如何");
    }
}
