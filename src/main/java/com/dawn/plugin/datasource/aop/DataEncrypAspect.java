package com.dawn.plugin.datasource.aop;

import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Aspect
@Order(12)
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public class DataEncrypAspect {

    @Pointcut("@within(com.dawn.plugin.datasource.aop.EncrypEntity))")
    public void withinDataEncrypAspect() {
        /* DataEncryp */
    }

    @Pointcut("execution(* com.dawn..*.mapper..*.*(..))")
    public void executionDataEncrypAspect() {
        /* none */
    }

    @SneakyThrows
    @Around("withinDataEncrypAspect()")
    public Object dataAround(ProceedingJoinPoint point) {
        log.debug(LogEnmu.LOG4.value(), "request",
                point.getSignature().getDeclaringTypeName(),
                point.getTarget().getClass().getName(),
                point.getSignature().getName());
        /* 获取参数列表 */
        Object[] args = point.getArgs();
        /* 获取方法 */
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Map<Integer, EncrypEntity> posMap = getPosMap(method);

        /* 参数加解密处理 */
        args = setArgs(args, posMap);

        /* proceed */
        Object reObj = point.proceed(args);

        /* 返回解密 */
        if (reObj instanceof Collection) {
            log.debug(LogEnmu.LOG2.value(), "start", "Collection");
        } else {
            log.debug(LogEnmu.LOG2.value(), "start", reObj.toString());
        }

        return reObj;
    }

    /**
     * [加密]
     *
     * @param data         [data]
     * @param encrypEntity [encrypEntity]
     * @return java.lang.String
     **/
    public String encrypData(String data, EncrypEntity encrypEntity) {
        if (AlgEnmu.BASE64.algorithm().equals(encrypEntity.encrypType())) {
            return Base64.isBase64(data) ? data : Base64.encodeBase64String(data.getBytes(StandardCharsets.UTF_8));
        } else {
            return data;
        }
    }

    /**
     * [解密]
     *
     * @param data         [data]
     * @param encrypEntity [encrypEntity]
     * @return java.lang.String
     **/
    public String decodeData(String data, EncrypEntity encrypEntity) {
        if (AlgEnmu.BASE64.algorithm().equals(encrypEntity.encrypType())) {
            return Base64.isBase64(data) ? new String(Base64.decodeBase64(data), StandardCharsets.UTF_8) : data;
        } else {
            return data;
        }
    }

    private Map<String, Method> getMethods(Object arg) {
        Method[] methods = arg.getClass().getMethods();
        Map<String, Method> methodMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        for (Method method : methods) {
            methodMap.put(method.getName().toLowerCase(), method);
        }
        return methodMap;
    }

    private Map<Integer, EncrypEntity> getPosMap(Method method) {
        Map<Integer, EncrypEntity> posMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        int ePos = 0;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation : annotations) {
                /* [辨别 int editByC1(String c1, @EncrypEntity String c2);] */
                if (annotation.annotationType().getTypeName().equals(EncrypEntity.class.getTypeName())) {
                    posMap.put(ePos, (EncrypEntity) annotation);
                }
            }
            ePos++;
        }
        return posMap;
    }

    private Object[] setArgs(Object[] args, Map<Integer, EncrypEntity> posMap) {
        /* 遍历所有的参数 */
        int ePos = 0;
        for (Object arg : args) {
            /* 辨别class是否需要加解密处理 */
            if (!Objects.isNull(arg.getClass().getAnnotation(EncrypEntity.class))) {
                Field[] fields = arg.getClass().getDeclaredFields();
                Map<String, Method> methodMap = getMethods(arg);
                for (Field field : fields) {
                    EncrypEntity encrypEntity = field.getAnnotation(EncrypEntity.class);
                    if (!Objects.isNull(encrypEntity)) {
                        /* 需要加密处理 */
                        Method getMethod = methodMap.get("get".concat(field.getName().toLowerCase()));
                        Method setMethod = methodMap.get("set".concat(field.getName().toLowerCase()));
                        Object val = ReflectionUtils.invokeMethod(getMethod, arg);
                        val = encrypData(String.valueOf(val), encrypEntity);
                        ReflectionUtils.invokeMethod(setMethod, arg, val);
                    }
                }
            } else if (!Objects.isNull(posMap.get(ePos))) {
                args[ePos] = encrypData(String.valueOf(arg), posMap.get(ePos));
            }
            ePos++;
        }
        return args;
    }

}
