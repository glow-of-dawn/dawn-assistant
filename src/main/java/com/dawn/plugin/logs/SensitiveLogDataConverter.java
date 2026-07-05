package com.dawn.plugin.logs;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.SensitiveUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;

/**
 * [自定义日志脱敏类]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public final class SensitiveLogDataConverter extends ClassicConverter {

    private String logSensitive = VarEnmu.DISABLE.value();
    private final int max = VarEnmu.THREE.ivalue();

    /**
     * convert
     *
     * @param event event
     * @return String
     */
    @Override
    public String convert(ILoggingEvent event) {
        var message = event.getMessage();
        var args = event.getArgumentArray();
        if (LogEnmu.LOG_SENSITIVE_STATUS.value().equals(message) && args.length == VarEnmu.ONE.ivalue()) {
            logSensitive = String.valueOf(args[0]);
            return "--+- LOG_SENSITIVE - [".concat(String.valueOf(args[0])).concat("] -+--");
        } else if (Objects.nonNull(message) && Objects.nonNull(args) && VarEnmu.ENABLE.value().equals(logSensitive)
                && args.length > VarEnmu.ZERO.ivalue()) {
            message = desensitization(message);
            int i = VarEnmu.ZERO.ivalue();
            for (var arg : args) {
                args[i] = desensitization(String.valueOf(arg));
                i++;
            }
            return MessageFormatter.arrayFormat(message, args).getMessage();
        } else {
            return event.getFormattedMessage();
        }
    }

    /**
     * 数据脱敏方法
     *
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    private String desensitization(String data) {
        if (Objects.isNull(data) || StringUtils.isBlank(data)) {
            return VarEnmu.NONE.value();
        }
        var dat = VarEnmu.NONE.value();
        int i = VarEnmu.ZERO.ivalue();
        /* 三次脱敏需要观察 */
        while (i < max) {
            i++;
            dat = SensitiveUtil.desensitization(data, SensitiveUtil.ID_CARD_REGEX,
                    VarEnmu.FOUR.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
            dat = SensitiveUtil.desensitization(dat, SensitiveUtil.BANK_CARD_REGEX,
                    VarEnmu.FOUR.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
            dat = SensitiveUtil.desensitization(dat, SensitiveUtil.MOBILE_REGEX,
                    VarEnmu.THREE.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
            dat = SensitiveUtil.desensitization(dat, SensitiveUtil.NAME_REGEX,
                    VarEnmu.ONE.ivalue(), VarEnmu.ZERO.ivalue(), VarEnmu.STAR.value());
        }
        return dat;
    }

}
