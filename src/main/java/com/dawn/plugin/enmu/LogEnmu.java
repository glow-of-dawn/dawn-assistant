package com.dawn.plugin.enmu;

/**
 * [日志]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public enum LogEnmu {

    /* [日志脱敏标识] */
    LOG_SENSITIVE_STATUS("LOG-SENSITIVE-STATUS"),
    /* [-+--] */
    LOG_LEFT("-+--"),
    /* [--+-] */
    LOG_RIGHT("--+-"),
    /* [{}] */
    LOG_BRACKETS("{}"),
    /* [-+-- {} --+-] */
    LOG1("-+-- {} --+-"),
    /* [-+-- [{}]:{} --+-] */
    LOG2("-+-- [{}]:{} --+-"),
    /* [-+-- [{}]:{} - {} --+-] */
    LOG3("-+-- [{}]:{} - {} --+-"),
    LOG3_1KV("-+-- {} - {}:{} --+-"),
    /* [-+-- [{}]:{} - {} - {} --+-] */
    LOG4("-+-- [{}]:{} - {} - {} --+-"),
    /* [默认通道] */
    LOG5("-+-- [{}]:{} - {} - {} - {} --+-"),
    LOG5_2KV("-+-- {} - {}:{} - {}:{} --+-"),
    /* [默认通道] */
    LOG6("-+-- [{}]:{} - {} - {} - {} - {} --+-"),
    /* [默认通道] */
    LOG7("-+-- [{}]:{} - {} - {} - {} - {} - {} --+-"),
    LOG7_3KV("-+-- {} - {}:{} - {}:{} - {}:{} --+-"),
    /* [默认通道] */
    LOG8("-+-- [{}]:{} - {} - {} - {} - {} - {} - {} --+-"),
    /* [默认通道] */
    LOG9("-+-- [{}]:{} - {} - {} - {} - {} - {} - {} - {} --+-"),
    LOG9_4KV("-+-- {} - {}:{} - {}:{} - {}:{} - {}:{} --+-"),
    /* [默认通道] */
    LOG10("-+-- [{}]:{} - {} - {} - {} - {} - {} - {} - {} - {} --+-"),
    EXCEPTION_ILLEGAL_ARGUMENT("java.lang.IllegalArgumentException: ");

    private final String value;

    LogEnmu(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public String pair(String tit, int pairSize) {
        StringBuilder sb = new StringBuilder();
        sb.append(LogEnmu.LOG_LEFT.value);
        sb.append(tit).append(VarEnmu.SPACE.value());
        int i = 0;
        while (i < pairSize) {
            sb.append(VarEnmu.SLIGHTLY.value()).append(VarEnmu.SPACE.value())
                    .append(LogEnmu.LOG_BRACKETS.value).append(VarEnmu.QUOTE.value()).append(LogEnmu.LOG_BRACKETS.value)
                    .append(VarEnmu.SPACE.value());
            i++;
        }
        sb.append(LogEnmu.LOG_RIGHT.value);
        return sb.toString();
    }

}
