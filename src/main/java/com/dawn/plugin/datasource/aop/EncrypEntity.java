package com.dawn.plugin.datasource.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface EncrypEntity {

    /* 开启令牌认证; 默认不开启 */
    String encrypType() default "BASE64";

}
