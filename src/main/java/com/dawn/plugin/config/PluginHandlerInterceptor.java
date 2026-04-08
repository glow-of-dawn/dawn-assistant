package com.dawn.plugin.config;

import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Component
public class PluginHandlerInterceptor implements HandlerInterceptor {

    @SneakyThrows
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setStatus(CodeEnmu.HTTP_463.icode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        var out = response.getWriter();
        out.print(String.format(VarEnmu.JSON_RESPONSE.value(), CodeEnmu.HTTP_463.icode(), VarEnmu.FALSE.value(), CodeEnmu.HTTP_463.description()));
        out.flush();
        out.close();
        log.debug(LogEnmu.LOG2.value(), "拦截处理", request);
        return false;
    }

}
