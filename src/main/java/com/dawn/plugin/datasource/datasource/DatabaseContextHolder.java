package com.dawn.plugin.datasource.datasource;

/**
 * 保存一个线程安全的DatabaseType容器
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public class DatabaseContextHolder {

    /* 用于存放多线程环境下的成员变量 */
    private static final ThreadLocal<DataType> CONTEXT_HOLDER = new ThreadLocal<>();

    private DatabaseContextHolder() {
        throw new IllegalStateException("assistant ---> private DatabaseContextHolder();");
    }

    protected static void remove() {
        CONTEXT_HOLDER.remove();
    }

    public static DataType getDatabaseType() {
        return CONTEXT_HOLDER.get();
    }

    public static void setDatabaseType(DataType type) {
        CONTEXT_HOLDER.set(type);
    }

}
