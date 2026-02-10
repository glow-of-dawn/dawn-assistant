package com.dawn.plugin.util;

import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * [自定义断言处理]
 * 创建时间：2021/2/24 14:36
 *
 * @author hforest-480s
 */
@Slf4j
public class PluginAssert extends Assert {

    /**
     * [含复杂记录日志处理]
     *
     * @param response [response]
     * @param message  [辅助信息]
     * @return Response
     **/
    public static void notHttp200(Response<Object> response, String message) {
        if (response == null) {
            log.info(LogEnmu.LOG2.value(), "NotHttp200", "response is null");
            throw new IllegalArgumentException(CodeEnmu.HTTP_498.codeIex(message));
        } else if (CodeEnmu.HTTP_200.icode() != response.getCode()) {
            log.info(LogEnmu.LOG3.value(), "NotHttp200", response.getCode(), response.getMessage());
            throw new IllegalArgumentException(new StringBuilder().append(response.getCode()).append("::").append(response.getMessage()).toString());
        }
    }

    /**
     * [普通处理]
     *
     * @param response [response]
     * @return Response
     **/
    public static void notHttp200(Response<Object> response) {
        notHttp200(response, VarEnmu.NONE.value());
    }

}
