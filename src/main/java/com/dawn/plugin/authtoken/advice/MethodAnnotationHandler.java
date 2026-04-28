package com.dawn.plugin.authtoken.advice;

import com.dawn.plugin.authtoken.Authtoken;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * 创建时间 2025/9/18 17:45
 *
 * @author bhyt2
 */
@Component
public class MethodAnnotationHandler {

    /**
     * [获取 Authtoken]
     *
     * @param methodParameter [methodParameter]
     * @return Authtoken
     */
    public Authtoken getAuthtokenByMethodParameter(MethodParameter methodParameter) {
        AtomicReference<Authtoken> automAtoken = new AtomicReference<>();
        Annotation[] annotations = methodParameter.getMethodAnnotations();
        Arrays.stream(annotations)
                .filter(Authtoken.class::isInstance)
                .findFirst()
                /* mvc 函数注解获取 */
                .ifPresent(ann -> automAtoken.set((Authtoken) ann));
        return automAtoken.get();
    }

}
