package com.dawn.plugin.enmu;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;

/**
 * [常用码对应]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public enum CodeEnmu {

    /* [初始值] */
    STS_A("A", "初始值"),
    /* [初始值] */
    STS_B("B", "回退处理"),
    /* [关闭 close] */
    STS_C("C", "关闭"),
    /* [状态] */
    STS_D("D", "D-状态"),
    /* [异常] */
    STS_E("E", "异常"),
    /* [失败] */
    STS_F("F", "失败"),
    /* [G-状态] */
    STS_G("G", "G-状态"),
    /* [H-状态] */
    STS_H("H", "H-状态"),
    /* [I-状态] */
    STS_I("I", "I-状态"),
    /* [J-状态] */
    STS_J("J", "J-状态"),
    /* [K-状态] */
    STS_K("K", "K-状态"),
    /* [L-状态] */
    STS_L("L", "L-状态"),
    /* [M-状态] */
    STS_M("M", "M-状态"),
    /* [N-状态] */
    STS_N("N", "N-状态"),
    /* [O-状态] */
    STS_O("O", "O-状态"),
    /* [P-状态] */
    STS_P("P", "P-状态"),
    /* [Q-状态] */
    STS_Q("Q", "Q-状态"),
    /* [R-状态] */
    STS_R("R", "R-状态"),
    /* [完成] */
    STS_S("S", "完成"),
    /* [T-状态] */
    STS_T("T", "T-状态"),
    /* [U-状态] */
    STS_U("U", "U-状态"),
    /* [V-状态] */
    STS_V("V", "V-状态"),
    /* [等待] */
    STS_W("W", "等待"),
    /* [X-状态] */
    STS_X("X", "X-状态"),
    /* [Y-状态] */
    STS_Y("Y", "Y-状态"),
    /* [Z-状态] */
    STS_Z("Z", "Z-状态"),
    /* [步骤 02] */
    STS_02("02", "步骤 02"),
    /* [步骤 03] */
    STS_03("03", "步骤 03"),
    /* [步骤 04] */
    STS_04("04", "步骤 04"),
    /* [步骤 05] */
    STS_05("05", "步骤 05"),
    /* [步骤 06] */
    STS_06("06", "步骤 06"),
    /* [程序处理完成] */
    HANDLE_OK(200, "程序处理完成"),
    /* [程序处理失败] */
    HANDLE_FAILURE(499, "程序处理失败"),
    /* [Content-Type] */
    CONTENT_TYPE("Content-Type", "content-type"),
    /* [transCode] */
    TRANS_CODE("transCode", "trans-code"),
    /* [平台服务] */
    HANDLER_IMPL("HandlerImpl", "-handler-impl"),
    /* [服务] */
    SERVICE_IMPL("ServiceImpl", "-service-impl"),
    /* [process] */
    PROCESS("process", "process-"),
    /* [trans] */
    TRANS("trans", "trans-"),
    /* [http.200] */
    HTTP_200(200, "http.200"),
    /* [http.260] */
    HTTP_260(260, "不须认证业务"),
    /* [http.261] */
    HTTP_261(261, "认证功能未开启"),
    /* [http.262] */
    HTTP_262(262, "权限功能未开启"),
    /* [http.299] */
    HTTP_299(299, "http.299"),
    /* [http.350] */
    HTTP_350(350, "未定义返回"),
    /* [http.455] */
    HTTP_455(455, "地址校验不成立"),
    /* [http.456] */
    HTTP_456(456, "没有获取您的令牌"),
    /* [http.457] */
    HTTP_457(457, "您的令牌不存在或者已经失效"),
    /* [http.458] */
    HTTP_458(458, "HASH校验失败"),
    /* [http.459] */
    HTTP_459(459, "重复请求[once]"),
    /* [http.460] */
    HTTP_460(460, "认证信息不全[headers]"),
    /* [http.461] */
    HTTP_461(461, "请求时间不在合理范围内[timestamp]"),
    /* [http.462] */
    HTTP_462(462, "服务端处理请求加解密失败：request.body"),
    /* [http.463] */
    HTTP_463(463, "请求被拦截"),
    /* [http.498: Assert 异常] */
    HTTP_498(498, "assert:"),
    /* [http.499] */
    HTTP_499(499, "响应处理失败"),
    /* [http.499] */
    HTTP_497(497, "PluginFeignClient 处理异常,常见PluginFeignClient处理的uri服务是否正常."),
    /* [30s 等待wait-time] */
    SYS_WAIT_TIME_30S(30, "wait-time");

    private final String code;
    private final int icode;
    private final String description;

    CodeEnmu(String code, String description) {
        this.code = code;
        this.icode = NumberUtils.toInt(code, VarEnmu.NUMBER_9999999.ivalue());
        this.description = description;
    }

    CodeEnmu(int icode, String description) {
        this.code = String.valueOf(icode);
        this.icode = icode;
        this.description = description;
    }

    public static String getDescription(Object xcode) {
        var ele = Arrays.stream(values())
            .filter(eodeEnmu -> eodeEnmu.code().equals(String.valueOf(xcode)))
            .findFirst();
        return ele.isEmpty() ? VarEnmu.NONE.value() : ele.get().description;
    }

    /**
     * [code]
     *
     * @return java.lang.String
     */
    public String code() {
        return this.code;
    }

    /**
     * [icode]
     *
     * @return int
     */
    public int icode() {
        return this.icode;
    }

    /**
     * [description]
     *
     * @return java.lang.String
     */
    public String description() {
        return this.description;
    }

    /**
     * [codeIex]
     *
     * @return java.lang.String
     */
    public String codeIex() {
        return this.code.concat("::").concat(this.description);
    }

    /**
     * [codeIex]
     *
     * @param msg [msg]
     * @return java.lang.String
     */
    public String codeIex(String msg) {
        return this.code.concat("::").concat(this.description.concat(msg));
    }

}
