package com.dawn.plugin.enmu;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public enum VarEnmu {

    /* [true] */
    TRUE("true"),
    /* [false] */
    FALSE("false"),
    /* [enable] */
    ENABLE("enable"),
    /* [disable] */
    DISABLE("disable"),
    /* [disable] */
    STATUS("status"),
    /* [console-field] */
    CONSOLE_FIELD("console-field"),
    /* [yes] */
    YES("yes"),
    /* [no] */
    NO("no"),
    /* [Y] */
    Y("Y"),
    /* [N] */
    N("N"),
    /* [success] */
    FAIL("fail"),
    /* [success] */
    SUCCESS("success"),
    /* [none] */
    NONE(""),
    /* [json] */
    JSON("json"),
    /* [{}] */
    JSON_NONE("{}"),
    /* [{}] */
    JSON_RESPONSE("{\"code\": %s, \"success\": %s, \"message\": \"%s\", \"data\": \"\"}"),
    /* [{] */
    JSON_LEFT("{"),
    /* [}] */
    JSON_RIGHT("}"),
    /* [xml] */
    XML("xml"),
    /* [xml] */
    XML_NONE("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>"),
    /* [xml] */
    XML_LEFT("<?xml"),
    /* [type] */
    TYPE("type"),
    /* [key] */
    KEY("key"),
    /* [value] */
    VALUE("value"),
    /* [val] */
    VAL("val"),
    /* [id] */
    ID("id"),
    /* [0] */
    ZERO(0),
    /* [1] */
    ONE(1),
    /* [2] */
    TWO(2),
    /* [3] */
    THREE(3),
    /* [4] */
    FOUR(4),
    /* [5] */
    FIVE(5),
    /* [6] */
    SIX(6),
    /* [7] */
    SEVEN(7),
    /* [8] */
    EIGHT(8),
    /* [9] */
    NINE(9),
    /* [10] */
    TEN(10),
    /* [11] */
    ELEVEN(11),
    /* [12] */
    TWELVE(12),
    /* [13] */
    THIRTEEN(13),
    /* [14] */
    FOURTEEN(14),
    /* [15] */
    FIFTEEN(15),
    /* [16] */
    SIXTEEN(16),
    /* [17] */
    SEVENTEEN(17),
    /* [18] */
    EIGHTEEN(18),
    /* [19] */
    NINETEEN(19),
    /* [20] */
    TWENTY(20),
    /* [30] */
    THIRTY(30),
    /* [50] */
    NUMBER_50(50),
    /* [60] */
    NUMBER_60(60),
    /* [64] */
    NUMBER_64(64),
    /* [99] */
    NUMBER_99(99),
    /* [100] */
    ONE_HUNDRED(100),
    /* [128] */
    NUMBER_128(128),
    /* [200] */
    NUMBER_200(200),
    /* [256] */
    NUMBER_256(256),
    /* [300] */
    NUMBER_300(300),
    /* [400] */
    NUMBER_400(400),
    /* [450] */
    NUMBER_450(450),
    /* [500] */
    NUMBER_500(500),
    /* [512] */
    NUMBER_512(512),
    /* [600] */
    NUMBER_600(600),
    /* [700] */
    NUMBER_700(700),
    /* [800] */
    NUMBER_800(800),
    /* [900] */
    NUMBER_900(900),
    /* [1000] */
    NUMBER_1000(1000),
    /* [1024] */
    NUMBER_1024(1024),
    /* [2000] */
    NUMBER_2000(2000),
    /* [2400] */
    NUMBER_2400(2400),
    /* [2048] */
    NUMBER_2048(2048),
    /* [3000] */
    NUMBER_3000(3000),
    /* [4096] */
    NUMBER_4096(4096),
    /* [5000] */
    NUMBER_5000(5000),
    /* [9000] */
    DEF_USERID(9000),
    /* [10000] */
    NUMBER_10000(10000),
    /* [9999999] */
    NUMBER_9999999(9999999),
    /* [0x0f0] */
    NUM_0X0F0(0x0f0),
    /* [0x0f] */
    NUM_0X0F(0x0f),
    /* [0xff] */
    NUM_0XFF(0xff),
    /* [magic.-1] */
    IIT_MINUS_ONE(-1),
    /* [timestamp] */
    TIMESTAMP("timestamp"),
    /* [yyyy-MM-dd HH:mm:ss] */
    DATE_TIME_FORMATTER("yyyy-MM-dd HH:mm:ss"),
    /* [yyyyMMddHHmmss] */
    DATE_TIME_FORMATTER_TERSE("yyyyMMddHHmmss"),
    /* [HH:mm:ss] */
    TIME_FORMATTER("HH:mm:ss"),
    /* [HHmmss] */
    TIME_FORMATTER_TERSE("HHmmss"),
    /* [yyyyMMddHHmmssSSS] */
    DATE_TIME_FORMATTER_TERSE_MILLIL("yyyyMMddHHmmssSSS"),
    /* [yyyy-MM-dd] */
    DATE_FORMATTER("yyyy-MM-dd"),
    /* [yyyyMMdd] */
    DATE_FORMATTER_TERSE("yyyyMMdd"),
    /* [space] */
    SPACE(" "),
    /* [*] */
    STAR("*"),
    /* [-] */
    SLIGHTLY("-"),
    /* [_] */
    UNDERLINE("_"),
    /* [+] */
    PLUS("+"),
    /* [|] */
    VLINE("|"),
    /* [.] */
    POINT("."),
    /* [,] */
    COMMA(","),
    /* [:] */
    QUOTE(":"),
    /* [::] */
    DOUBLE_QUOTE("::"),
    /* [FTF-8] */
    UTF8("UTF-8"),
    /* [FTF-8] */
    ISO_8859_1("ISO_8859_1"),
    /* [GBK] */
    GBK("GBK"),
    /* [ON] */
    ON("ON"),
    /* [OFF] */
    OFF("OFF"),
    /* [OPEN] */
    OPEN("OPEN"),
    /* [CLOSE] */
    CLOSE("CLOSE"),
    /* [/] */
    SLASH("/"),
    /* [/] */
    LF("\n"),
    /* [/] */
    CRLF("\r\n"),
    /* [\] */
    BACK_SLASH("\\"),
    /* [私钥常量] */
    PRIVATE_KEY_HEX("private-key-hex"),
    /* [私钥常量] */
    PRIVATE_KEY("private-key"),
    /* [公钥常量] */
    PUBLIC_KEY_HEX("public-key-hex"),
    /* [公钥常量] */
    PUBLIC_KEY("public-key"),
    /* [end] */
    END("END"),
    /* [begin] */
    BEGIN("BEGIN"),
    /* [client] */
    CLIENT("client"),
    /* [token] */
    TOKEN("token"),
    /* [auth-token] */
    AUTH_TOKEN("auth-token"),
    /* [authtoken] */
    AUTHTOKEN("authtoken"),
    /* [session-id] */
    SESSION_ID("session-id"),
    /* [userid] */
    USERID("userid"),
    /* [data] */
    DATA("data"),
    /* [date] */
    DATE("date"),
    /* [time] */
    TIME("time"),
    /* [datetime] */
    DATE_TIME("datetime"),
    /* [code] */
    CODE("code"),
    /* [message] */
    MESSAGE("message"),
    /* [msg] */
    MSG("msg"),
    /* [response] */
    RESPONSE("response"),
    /* [request] */
    REQUEST("request"),
    /* [body] */
    BODY("body"),
    /* [headers] */
    HEADERS("headers"),
    /* [group-id] */
    GROUP_ID("group-id"),
    /* [delete] */
    DELETE("delete"),
    /* [remove] */
    REMOVE("remove"),
    /* [select] */
    SELECT("select"),
    /* [find] */
    FIND("find"),
    /* [edit] */
    EDIT("edit"),
    /* [update] */
    UPDATE("update"),
    /* [insert] */
    INSERT("insert"),
    /* [create] */
    CREATE("create"),
    /* [payload] */
    PAYLOAD("payload"),
    /* [null] */
    NULL("null"),
    /* [timeout] */
    TIMEOUT("timeout"),
    /* [master] */
    MASTER("master"),
    /* [timeout] */
    SLAVE("slave"),
    /* [filterKey] */
    CONSUMER_REDIS_STREAM_LISTENER("ConsumerRedisStreamListener"),
    /* [服务名称] */
    SERVICE_NAME("service-name"),
    /* [消费状态:enable disable] */
    CONSUMER_STATUS("consumer-status"),
    /* [roundNo.incrementRroundNo] */
    INCREMENT_RROUND_NO("increment-round-no"),
    /* [illegalArgumentException] */
    ILLEGAL_ARGUMENT_EXCEPTION_LOG("java.lang.IllegalArgumentException: ");

    private final String value;
    private final int ivalue;

    VarEnmu(String value) {
        this.value = value;
        this.ivalue = NumberUtils.toInt(value, 0);
    }

    VarEnmu(int value) {
        this.value = String.valueOf(value);
        this.ivalue = value;
    }

    public String value() {
        return this.value;
    }

    public int ivalue() {
        return this.ivalue;
    }

}
