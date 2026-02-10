package com.dawn.plugin.util;

import com.dawn.plugin.enmu.VarEnmu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 创建时间：2021/2/1 11:35
 *
 * @author hforest-480s
 */
public class RandomUtil {

    private static final Random RANDOM = new Random();

    private RandomUtil() {
    }

    /**
     * 获取 count 位随机整数  0~9
     *
     * @param count [count]
     * @return int
     */
    public static int getRandomInt(int count) {
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < count; i++) {
            num.append(RANDOM.nextInt(VarEnmu.NINE.ivalue()));
        }
        return count == 0 ? 0 : Integer.parseInt(num.toString());
    }

    /**
     * 获取 count 位随机整数  0~long.max long
     *
     * @param count [count]
     * @return long
     */
    public static long getRandomLong(int count) {
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < count; i++) {
            num.append(RANDOM.nextInt(VarEnmu.NINE.ivalue()));
        }
        return count == 0 ? 0 : Long.parseLong(num.toString());
    }

    /**
     * 取不重复随机数列
     *
     * @param srcRange 取的范围
     * @param count    取的个数
     * @return List<Integer>
     */
    public static List<Integer> getRandomIntList(List<Integer> srcRange, int count) {
        var cnt = count;
        List<Integer> list = new ArrayList<>();
        if (cnt > srcRange.size()) {
            return list;
        }
        List<Integer> range = new ArrayList<>(srcRange);

        int size = range.size();
        List<Integer> intList = new ArrayList<>();
        while (cnt > 0) {
            int index = RANDOM.nextInt(size);
            intList.add(range.get(index));
            range.remove(index);
            size--;
            cnt--;
        }
        return intList;
    }

    /**
     * 取随机对象
     *
     * @param srcRange 范围
     * @param count    取数个数
     * @param isRepeat 是否可以取重复对象
     * @return List<Object>
     */
    public static List<Object> getRandomList(List<Object> srcRange, int count, boolean isRepeat) {
        List<Object> list = new ArrayList<>();
        var cnt = count;
        if (cnt > srcRange.size()) {
            return list;
        }
        List<Object> range = new ArrayList<>(srcRange);
        int size = range.size();
        while (cnt > 0) {
            int index = RANDOM.nextInt(size);
            list.add(range.get(index));
            if (!isRepeat) {
                range.remove(index);
                size--;
            }
            cnt--;
        }
        return list;
    }

    /**
     * 获取 charLength 长度的随机码 采用chars中的值
     *
     * @param srcChars   无需分隔符
     * @param charLength 取值长度
     * @param isRepeat   是否可以重复 当 chars.length() < charLength return null
     * @return String
     */
    public static String getRandomChar(StringBuilder srcChars, int charLength, boolean isRepeat) {
        if (srcChars.length() < charLength) {
            return "";
        }
        StringBuilder chars = new StringBuilder(srcChars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charLength; i++) {
            int idx = RANDOM.nextInt(chars.length());
            sb.append(chars.charAt(idx));
            if (!isRepeat) {
                chars.deleteCharAt(idx);
            }
        }
        return sb.toString();
    }

    /**
     * 获取 charLength 长度的随机码 A~Z a~z 0~9
     *
     * @param charLength [charLength]
     * @param isRepeat   是否可以重复 当 chars.length() < charLength return null
     * @return String
     */
    public static String getRandomChar(int charLength, boolean isRepeat) {
        return getRandomChar(new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"), charLength, isRepeat);
    }

    /**
     * 获取 charLength 长度的随机码 A~Z a~z 0~9
     *
     * @param charLength [charLength]
     * @return String
     */
    public static String getRandomChar(int charLength) {
        return getRandomChar(new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"), charLength, true);
    }

    /**
     * 获取 charLength 长度的随机码 A~Z a~z 0~9
     *
     * @param charLength [charLength]
     * @return String
     */
    public static String getRandomHexChar(int charLength, boolean isRepeat) {
        return getRandomChar(new StringBuilder("abcdef1234567890"), charLength, isRepeat);
    }

    /**
     * 获取 charLength 长度的随机码 A~Z a~z 0~9
     *
     * @param charLength [charLength]
     * @return String
     */
    public static String getRandomHexChar(int charLength) {
        return getRandomChar(new StringBuilder("abcdef1234567890"), charLength, true);
    }

}
