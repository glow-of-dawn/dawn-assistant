package com.dawn.plugin;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.thread.TestSimpleTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.application.name=dawn-assistant-test",
    "server.port=8080",
    "plugin-params.rest-client-url=http://localhost:8080/mock",
    "plugin-rest-controller.assistant-status=enable"
})
class AssistantServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PluginConfig config;

    @MockBean
    private PluginRestClient pluginRestClient;

    @MockBean
    private TabServerMapper tabServerMapper;

    @MockBean
    private TestSimpleTask testSimpleTask;

    @MockBean
    private RedisKeyService redisKeyService;

    @BeforeEach
    void setUp() {
        when(config.getApplicationId()).thenReturn("app-001");
        when(config.getMapperLowerCamel()).thenReturn(JsonMapper.builder().build());
        when(config.getSsrfHostWhiteList()).thenReturn(new HashSet<>());
        when(config.getSsrfPathWhiteList()).thenReturn(new HashSet<>());
        when(redisKeyService.isRedisHealth()).thenReturn(true);
    }

    @Test
    void getServiceInfo_shouldReturnApplicationInfo() throws Exception {
        mockMvc.perform(get("/rest/assistant/service/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("app-001"))
            .andExpect(jsonPath("$.message").value("dawn-assistant-test"));
    }

    @Test
    void healthRead_shouldReturnServiceInfoWhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/rest/assistant/service/health-read").header("health", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.applicationId").value("app-001"))
            .andExpect(jsonPath("$.data.serverName").value("dawn-assistant-test"));
    }

    @Test
    void setSsrfWhiteList_shouldAddHostAndPath() throws Exception {
        String body = "{\"api.example.com\":\"/health\",\"login.example.com\":\"/login\"}";

        mockMvc.perform(post("/rest/assistant/service/http/clinet/ssrf/white/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.host[0]").value("api.example.com"))
            .andExpect(jsonPath("$.data.path[0]").value("/health"));
    }

    @Test
    void logSensitive_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/rest/assistant/service/log-sensitive/13800138000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("日志脱敏:13800138000"));
    }
}
