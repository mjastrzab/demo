package com.example.demo.utils;

import lombok.NonNull;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public final class MathContext {

    public static java.math.MathContext DIGITS_2 = new java.math.MathContext(2, HALF_UP);

    public static BigDecimal add(@NonNull BigDecimal first, @NonNull BigDecimal second) {
        return first.add(second, DIGITS_2);
    }

    public static BigDecimal subtract(@NonNull BigDecimal first, @NonNull BigDecimal second) {
        return first.subtract(second, DIGITS_2);
    }
}
