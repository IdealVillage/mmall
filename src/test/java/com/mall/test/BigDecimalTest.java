package com.mall.test;

import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalTest {
    @Test
    public void test(){
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }
}
