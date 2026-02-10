package com.dawn.plugin.util;

import com.dawn.plugin.enmu.VarEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * [多值返回]
 * 创建时间：2021/3/31 21:27
 *
 * @author hforest-480s
 */
@Data
@Slf4j
public class Multivalued<T> {

    private boolean success = false;
    private T data0;
    private T data1;
    private T data2;
    private T data3;
    private T data4;
    private T data5;
    private T data6;
    private T data7;
    private T data8;
    private T data9;
    private T data10;
    private T data11;
    private T data12;
    private T data13;
    private T data14;
    private T data15;

    public Multivalued() {
        data0 = (T) VarEnmu.NONE.value();
        data1 = (T) VarEnmu.NONE.value();
        data2 = (T) VarEnmu.NONE.value();
        data3 = (T) VarEnmu.NONE.value();
        data4 = (T) VarEnmu.NONE.value();
        data5 = (T) VarEnmu.NONE.value();
        data6 = (T) VarEnmu.NONE.value();
        data7 = (T) VarEnmu.NONE.value();
        data8 = (T) VarEnmu.NONE.value();
        data9 = (T) VarEnmu.NONE.value();
        data10 = (T) VarEnmu.NONE.value();
        data11 = (T) VarEnmu.NONE.value();
        data12 = (T) VarEnmu.NONE.value();
        data13 = (T) VarEnmu.NONE.value();
        data14 = (T) VarEnmu.NONE.value();
        data15 = (T) VarEnmu.NONE.value();
    }

    public Multivalued<T> success() {
        this.success = true;
        return this;
    }

    public Multivalued<T> failure() {
        this.success = false;
        return this;
    }

    public <V> V getData0() {
        return (V) this.data0;
    }

    public <V> V getData1() {
        return (V) this.data1;
    }

    public <V> V getData2() {
        return (V) this.data2;
    }

    public <V> V getData3() {
        return (V) this.data3;
    }

    public <V> V getData4() {
        return (V) this.data4;
    }

    public <V> V getData5() {
        return (V) this.data5;
    }

    public <V> V getData6() {
        return (V) this.data6;
    }

    public <V> V getData7() {
        return (V) this.data7;
    }

    public <V> V getData8() {
        return (V) this.data8;
    }

    public <V> V getData9() {
        return (V) this.data9;
    }

    public <V> V getData10() {
        return (V) this.data10;
    }

    public <V> V getData11() {
        return (V) this.data11;
    }

    public <V> V getData12() {
        return (V) this.data12;
    }

    public <V> V getData13() {
        return (V) this.data13;
    }

    public <V> V getData14() {
        return (V) this.data14;
    }

    public <V> V getData15() {
        return (V) this.data15;
    }

    public Multivalued<T> data0(T data0) {
        this.data0 = data0;
        return this;
    }

    public Multivalued<T> data1(T data1) {
        this.data1 = data1;
        return this;
    }

    public Multivalued<T> data2(T data2) {
        this.data2 = data2;
        return this;
    }

    public Multivalued<T> data3(T data3) {
        this.data3 = data3;
        return this;
    }

    public Multivalued<T> data4(T data4) {
        this.data4 = data4;
        return this;
    }

    public Multivalued<T> data5(T data5) {
        this.data5 = data5;
        return this;
    }

    public Multivalued<T> data6(T data6) {
        this.data6 = data6;
        return this;
    }

    public Multivalued<T> data7(T data7) {
        this.data7 = data7;
        return this;
    }

    public Multivalued<T> data8(T data8) {
        this.data8 = data8;
        return this;
    }

    public Multivalued<T> data9(T data9) {
        this.data9 = data9;
        return this;
    }

    public Multivalued<T> data10(T data10) {
        this.data10 = data10;
        return this;
    }

    public Multivalued<T> data11(T data11) {
        this.data11 = data11;
        return this;
    }

    public Multivalued<T> data12(T data12) {
        this.data12 = data12;
        return this;
    }

    public Multivalued<T> data13(T data13) {
        this.data13 = data13;
        return this;
    }

    public Multivalued<T> data14(T data14) {
        this.data14 = data14;
        return this;
    }

    public Multivalued<T> data15(T data15) {
        this.data15 = data15;
        return this;
    }

}
