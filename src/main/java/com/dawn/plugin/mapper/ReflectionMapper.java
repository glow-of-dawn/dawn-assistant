package com.dawn.plugin.mapper;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 * 服务层反射工具类
 *
 * @author hforest-480s
 */
@Slf4j
@Component
public class ReflectionMapper {

    private final PluginConfig config;

    public ReflectionMapper(PluginConfig config) {
        this.config = config;
    }

    /**
     * [反射执行]
     *
     * @param body [body is json]
     * @return Object
     **/
    public Object invokeMethod(String body) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> tabMap = config.getMapperLowerCamel().readValue(body, Map.class);
        AtomicReference<String> atomMapperName = new AtomicReference<>(VarEnmu.NONE.value());
        config.getBeans().stream()
            .filter(name -> name.contains("Mapper"))
            .filter(name -> name.contains(String.valueOf(tabMap.get(VarEnmu.TABLE_NAME.value()))))
            .forEach(atomMapperName::set);
        log.debug(LogEnmu.LOG2.value(), VarEnmu.TABLE_NAME.value(), atomMapperName.get());
        var mapper = config.getComponentServiceBean(atomMapperName.get());
        Assert.notNull(mapper, "mapper is null!");
        var methodFind = ReflectionUtils.findMethod(mapper.getClass(), VarEnmu.FIND.value(), String.class);
        methodFind = Objects.isNull(methodFind) ? ReflectionUtils.findMethod(mapper.getClass(), VarEnmu.FIND.value(), Object.class) : methodFind;
        Assert.notNull(methodFind, "methodFind is null!");
        var methods = ReflectionUtils.getDeclaredMethods(mapper.getClass());
        AtomicReference<Method> atomMethod = new AtomicReference<>();
        return switch (String.valueOf(tabMap.get(VarEnmu.METHOD_TYPE.value()))) {
            case "create" -> {
                var en = methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
                Assert.isNull(en, "entity id already have!");
                Arrays.stream(methods)
                    .filter(method -> method.getName().equals(VarEnmu.CREATE.value()))
                    .forEach(atomMethod::set);
                Optional.ofNullable(atomMethod.get())
                    .ifPresent(methodCreate -> {
                        var classType = methodCreate.getParameterTypes()[VarEnmu.ZERO.ivalue()];
                        var entity = config.getMapperLowerCamel().convertValue(tabMap, classType);
                        ReflectionUtils.invokeMethod(methodCreate, mapper, entity);
                    });
                yield methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
            }
            case "edit" -> {
                Arrays.stream(methods)
                    .filter(method -> method.getName().equals(VarEnmu.EDIT.value()))
                    .forEach(atomMethod::set);
                Assert.notNull(atomMethod.get(), "methodEdit is null!");
                var methodEdit = atomMethod.get();
                var entity = methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
                Assert.notNull(atomMethod.get(), "entity is null!");
                Map<String, Field> fieldMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
                Arrays.asList(entity.getClass().getDeclaredFields()).forEach(field -> fieldMap.put(field.getName(), field));
                tabMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(VarEnmu.ID.value()))
                    .filter(entry -> fieldMap.containsKey(entry.getKey()))
                    .forEach(entry -> {
                        Field field = fieldMap.get(entry.getKey());
                        String setter = "set" + capitalize(field.getName());
                        Object val;
                        if (field.getType() == LocalDateTime.class) {
                            val = LocalDateTime.parse(String.valueOf(entry.getValue()),
                                DateTimeFormatter.ofPattern(VarEnmu.DATE_TIME_FORMATTER.value()));
                        } else if (field.getType() == LocalDate.class) {
                            val = LocalDate.parse(String.valueOf(entry.getValue()), DateTimeFormatter.ofPattern(VarEnmu.DATE_FORMATTER.value()));
                        } else if (field.getType() == BigDecimal.class) {
                            val = new BigDecimal(String.valueOf(entry.getValue()));
                        } else {
                            val = entry.getValue();
                        }
                        var setMethod = ReflectionUtils.findMethod(entity.getClass(), setter, field.getType());
                        Assert.notNull(setMethod, "setMethod is null!");
                        ReflectionUtils.invokeMethod(setMethod, entity, val);
                    });
                methodEdit.invoke(mapper, entity);
                yield methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
            }
            case "destroy" -> {
                Arrays.stream(methods)
                    .filter(method -> method.getName().equals(VarEnmu.REMOVE.value()))
                    .forEach(atomMethod::set);
                Assert.notNull(atomMethod.get(), "methodRemove is null!");
                var entity = methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
                Optional.ofNullable(entity)
                    .ifPresent(en -> ReflectionUtils.invokeMethod(atomMethod.get(), mapper, tabMap.get(VarEnmu.ID.value())));
                yield entity;
            }
            default -> methodFind.invoke(mapper, tabMap.get(VarEnmu.ID.value()));
        };
    }

}
