package com.dawn.plugin.datasource.datasource;

import java.time.Instant;
import java.util.Objects;

/**
 * 数据源类型
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public class DataType {

    private final String name;
    private Long timestamp = 1L;
    private boolean action = true;

    public DataType(String name) {
        this.name = name;
    }

    public void pushTimestamp() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String getName() {
        return name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
        if (!action) {
            /* 如果为连接断裂则置于队列底 */
            this.timestamp = Instant.now().toEpochMilli() * 2;
        } else {
            /* 否则恢复连接 */
            this.timestamp = Instant.now().toEpochMilli();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataType dataType = (DataType) o;
        return name.equals(dataType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DataType{name=".concat(name)
                .concat(", timestamp=").concat(timestamp.toString())
                .concat(", action=").concat(String.valueOf(action))
                .concat("}");
    }
}
