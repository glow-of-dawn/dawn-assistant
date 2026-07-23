package com.ycmvp.plugin.enmu;

/**
 * [常用正则表达式]
 * 创建时间：2026/6/11 14:24
 *
 * @author hforest-480s
 */
public enum RegexEnmu {

    /* 纯数字 */
    NUMBER("\\d+"),
    /* 纯字母 */
    LETTER("[a-zA-Z]+"),
    /* 纯大写字母 */
    LETTER_UPPER_CASE("[A-Z]+"),
    /* 纯小字母 */
    LETTER_LOWER_CASE("[a-z]+"),
    /* 字母+数字 */
    NUMBER_AND_LETTER("[a-zA-Z0-9]+"),
    /* 字母+数字+/- */
    NUMBER_AND_LETTER_PATH("[a-zA-Z0-9/_-]+");

    private final String regex;

    RegexEnmu(String regex) {
        this.regex = regex;
    }

    public String regex() {
        return this.regex;
    }

}
