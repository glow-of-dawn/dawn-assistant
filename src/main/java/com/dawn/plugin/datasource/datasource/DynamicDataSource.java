package com.dawn.plugin.datasource.datasource;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DynamicDataSource
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    static final Map<String, Set<String>> PACKAGE_8_METHODS_MAP = new HashMap<>();
    static final Map<String, List<DataType>> PACKAGE_METHOD_8_DATASOURCE_MAP = new HashMap<>();
    static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<>();
    /* 全包包含对应表 */
    private static final Map<String, List<DataType>> PACKAGE_ALL_DATASOURCE_MAP = new HashMap<>();

    /**
     * [获取动态数据源信息]
     * return java.util.Map
     */
    public static Map<String, Object> getDynamicDataSource() {
        log.info(LogEnmu.LOG1.value(), "获取动态数据源信息");
        log.info(LogEnmu.LOG1.value(), "PACKAGE_METHOD_8_DATASOURCE_MAP");
        DynamicDataSource.PACKAGE_METHOD_8_DATASOURCE_MAP.forEach((k, v) ->
                log.info(LogEnmu.LOG4.value(), "packageName-defs", k, "datasource.1", v)
        );
        log.info(LogEnmu.LOG1.value(), "PACKAGE_8_METHODS_MAP");
        DynamicDataSource.PACKAGE_8_METHODS_MAP.forEach((k, v) -> log.info(LogEnmu.LOG4.value(), "packageName.2", k, "defs", v));
        log.info(LogEnmu.LOG1.value(), "PACKAGE_ALL_DATASOURCE_MAP");
        DynamicDataSource.PACKAGE_ALL_DATASOURCE_MAP.forEach((k, v) ->
                log.info(LogEnmu.LOG4.value(), "packageName.3", k, "datasource.3", v)
        );
        log.info(LogEnmu.LOG1.value(), "getDynamicDataSource over");
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.THREE.ivalue());
        map.put("PACKAGE_METHOD_8_DATASOURCE_MAP", DynamicDataSource.PACKAGE_METHOD_8_DATASOURCE_MAP);
        map.put("PACKAGE_8_METHODS_MAP", DynamicDataSource.PACKAGE_8_METHODS_MAP);
        map.put("PACKAGE_ALL_DATASOURCE_MAP", DynamicDataSource.PACKAGE_ALL_DATASOURCE_MAP);
        return map;
    }

    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        DataType type = DatabaseContextHolder.getDatabaseType();
        log.debug(LogEnmu.LOG2.value(), "dataSource.4", type);
        return type.getName();
    }

    /**
     * [登记数据源]
     * param dataType [DataType]
     * return void
     **/
    public static void setDataTypeMap(DataType dataType) {
        DATA_TYPE_MAP.put(dataType.getName(), dataType);
    }

    /**
     * [设置数据源]
     * param datasource [datasource]
     * param packageName [packageName]
     * param defs [defs]
     * return void
     **/
    public void setMethodType(DataType dataType, String packageName, List<String> defs) {
        DATA_TYPE_MAP.put(dataType.getName(), dataType);
        log.debug(LogEnmu.LOG6.value(), "datasource.5", dataType, "packageName.5", packageName, "defs", defs);
        if (defs.contains(VarEnmu.STAR.value())) {
            List<DataType> ds = PACKAGE_ALL_DATASOURCE_MAP.computeIfAbsent(packageName, k -> new ArrayList<>());
            if (!ds.contains(dataType)) {
                ds.add(dataType);
            }
        }
        /* package-function: datasourceList */
        for (String defName : defs) {
            String packageDefPath = packageName.concat("-").concat(defName);
            List<DataType> datasources = PACKAGE_METHOD_8_DATASOURCE_MAP.computeIfAbsent(packageDefPath, k -> new ArrayList<>());
            if (!datasources.contains(dataType)) {
                datasources.add(dataType);
            }
        }
        /* package & methods ---> PACKAGE_8_METHODS_MAP */
        Set<String> pms = PACKAGE_8_METHODS_MAP.computeIfAbsent(packageName, k -> new HashSet<>());
        defs.remove("*");
        pms.addAll(defs);
        /* 全量数据更新 */
        PACKAGE_METHOD_8_DATASOURCE_MAP.forEach((k, v) -> {
            String pdPath = k.split("-")[0];
            List<DataType> ds = PACKAGE_ALL_DATASOURCE_MAP.get(pdPath);
            if (ds == null) {
                return;
            }
            for (DataType dt : ds) {
                if (!v.contains(dt)) {
                    v.add(dataType);
                }
            }
        });
    }

}
