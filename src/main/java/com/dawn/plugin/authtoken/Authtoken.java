package com.dawn.plugin.authtoken;

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
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Authtoken {

    /* 开启令牌认证; 默认不开启 */
    boolean openAuthtoken() default false;

    /* 必须进行加解密处理; 需要搭配 openAuthtoken 一并使用 */
    boolean openEncrypt() default false;

    /* 必须进行验签处理; 默认不开启 */
    boolean openSignature() default false;

    /* 必须进行权限校验; 需要搭配 openAuthtoken 一并使用 */
    boolean openRight() default false;

}
