package com.dawn.plugin.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * [bean]
 * 创建时间：2021/8/6 22:24
 *
 * @author hforest-480s
 */
@Slf4j
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * [获取空属性]
     *
     * @param source [source]
     * @return String[]
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        var result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
