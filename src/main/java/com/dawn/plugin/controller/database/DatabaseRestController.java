import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

import com.dawn.plugin.authtoken.Authtoken;
import com.ycmvp.plugin.config.PluginConfig;
import com.ycmvp.plugin.mapper.ReflectionMapper;

/**
 * [database服务]
 * 创建时间：2021/5/30 20:10
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/database/service")
@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
public class DatabaseRestController {

    private final PluginConfig config;
    private final ConvertUtil convertUtil;
    private final TempMapper tempMapper;
    private final TabServerMapper tabServerMapper;
    private final TabParamsMapper tabParamsMapper;
    private final ReflectionMapper reflectionMapper;
    
    public DatabaseRestController(PluginConfig config,
                                  ConvertUtil convertUtil,
                                  TempMapper tempMapper,
                                  TabServerMapper tabServerMapper,
                                  TabParamsMapper tabParamsMapper,
                                  ReflectionMapper reflectionMapper) {
        this.config = config;
        this.convertUtil = convertUtil;
        this.tempMapper = tempMapper;
        this.tabServerMapper = tabServerMapper;
        this.tabParamsMapper = tabParamsMapper;
        this.reflectionMapper = reflectionMapper;
    }

    /**
     * [数据调整]
     *
     * @param body 请求体内容
     * @return Object
     **/
    @PostMapping("/edit/tab/temp")
    public Object editHandler(@RequestBody String body) throws JsonProcessingException {
        Map<String, Object> entityMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var id = entityMap.getOrDefault(VarEnmu.ID.value(), VarEnmu.NONE.value());
        var temp = tempMapper.find(String.valueOf(id));
        Assert.notNull(temp, "temp is null!");
        if (!convertUtil.editEntity(entityMap, temp, "temp")) {
            tempMapper.edit(temp);
        }
        return new Response<>().success().data(temp);
    }

    @GetMapping("/persistence/test")
    public Response<Object> persistence() {
        var temps = tempMapper.findAll();
        var tabServers = tabServerMapper.findByApplicationSts(CodeEnmu.STS_A.code());
        return new Response<>()
            .success()
            .data(Map.of(
                "temps", temps,
                "tabServers", tabServers));
    }


    /**
     * [tabParams变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    @Authtoken(openAuthtoken = true)
    @SneakyThrows
    @PostMapping("/edit/params")
    public Response<Object> editTabParams(@RequestBody String body) {
        Map<String, Object> tabParamsMap = config.getMapperLowerCamel().readValue(body, Map.class);
        TabParams tabParams = config.getMapperLowerCamel().convertValue(tabParamsMap, TabParams.class);
        if (Objects.isNull(tabParamsMapper.find(tabParams.getId()))) {
            tabParamsMapper.create(tabParams);
        } else if (tabParamsMap.containsKey(VarEnmu.DELETE.value())) {
            tabParamsMapper.remove(tabParams.getId());
        } else if (tabParamsMap.size() == VarEnmu.ONE.ivalue()) {
            log.debug(LogEnmu.LOG1.value(), "查询");
        } else {
            tabParamsMapper.edit(tabParams);
        }
        tabParams = tabParamsMapper.find(tabParams.getId());
        return new Response<>().data(tabParams).success();
    }

    /**
     * [mapper变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    @Authtoken(openAuthtoken = true)
    @PostMapping("/mapper")
    public Response<Object> mapper(@RequestBody String body) throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        return new Response<>().data(reflectionMapper.invokeMethod(body)).success();
    }

}
