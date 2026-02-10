package com.dawn.plugin.util;

import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Instant;

/**
 * 创建时间：2020/12/29 16:08
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Accessors(chain = true)
public class Response<T> {

    private int code = CodeEnmu.HTTP_200.icode();
    private boolean success = false;
    private Long timestamp;
    private String message = CodeEnmu.HTTP_200.description();
    private T data;

    public Response() {
        data = (T) VarEnmu.NONE.value();
        timestamp = Instant.now().toEpochMilli();
    }

    /**
     * [成功返回请一定触发此函数]
     *
     * @return Response
     **/
    public Response<T> success() {
        this.success = true;
        this.code = CodeEnmu.HTTP_200.icode();
        this.message = CodeEnmu.HTTP_200.description();
        return this;
    }

    public Response<T> failure(String message) {
        if (message.contains(VarEnmu.DOUBLE_QUOTE.value())) {
            String str = message.replace(VarEnmu.ILLEGAL_ARGUMENT_EXCEPTION_LOG.value(), VarEnmu.NONE.value());
            String[] strs = str.split(VarEnmu.DOUBLE_QUOTE.value());
            this.code = NumberUtils.createInteger(strs[0]);
            this.message = strs.length > VarEnmu.ONE.ivalue() ? strs[1].trim() : VarEnmu.NONE.value();
            this.success = false;
            return this;
        } else {
            this.code = CodeEnmu.HTTP_499.icode();
            this.message = message.replace(VarEnmu.ILLEGAL_ARGUMENT_EXCEPTION_LOG.value(), VarEnmu.NONE.value());
            this.success = false;
            return this;
        }
    }

    public Response<T> codeMessage(int code) {
        this.code = code;
        this.message = CodeEnmu.getDescription(code);
        this.success = this.code == CodeEnmu.HTTP_200.icode();
        return this;
    }

    public Response<T> data(T data) {
        this.data = data;
        return this;
    }

    public <V> V getData() {
        return (V) this.data;
    }

    public Response<T> message(String message) {
        this.message = message;
        return this;
    }

    public Response<T> code(int code) {
        this.code = code;
        this.success = this.code == CodeEnmu.HTTP_200.icode();
        return this;
    }

    /**
     * [断言处理返回]
     * [code::] 本包包含异常处理
     * [code::message] 含有code.message异常，主要为非本包包含异常处理
     * [message] 直接信息异常处理
     * [直接暴漏处理] = [java.lang.IllegalArgumentException: xxxxxxxxxxx]
     *
     * @param ex [依赖于 spring Assert Throwable IllegalArgumentException]
     * @return Response
     **/
    public Response<T> assertException(IllegalArgumentException ex) {
        String info = ex.toString().replace("java.lang.IllegalArgumentException: ", VarEnmu.NONE.value());
        String[] inc = info.split("::");
        String des = CodeEnmu.getDescription(inc[0]);
        this.success = false;
        log.warn(LogEnmu.LOG2.value(), "断言处理", info);
        if (inc.length == 1 && !VarEnmu.NONE.value().equals(des) && NumberUtils.isCreatable(inc[0])) {
            /* 本包:[code{数字}::] */
            this.code = NumberUtils.toInt(inc[0]);
            this.message = CodeEnmu.getDescription(inc[0]);
        } else if (inc.length == 1 && !VarEnmu.NONE.value().equals(des)) {
            /* 本包:[code{非数字}::] */
            this.message = CodeEnmu.getDescription(inc[0]);
        } else if (inc.length == 1) {
            this.message = inc[0];
        } else if (inc.length == VarEnmu.TWO.ivalue() && NumberUtils.isCreatable(inc[0])) {
            /* 非本包:[code{数字}::message] */
            this.code = NumberUtils.toInt(inc[0]);
            this.message = inc[1];
        } else if (inc.length == VarEnmu.TWO.ivalue()) {
            /* 非本包||直接返回[message::xxx] or [code{非数字}::message] */
            this.message = info;
        } else {
            this.message = ex.toString();
        }
        return this;
    }

}
