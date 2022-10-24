package com.mg.trading.boot.utils;

import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

public class NumberUtils {

    public static BigDecimal defaultToZero(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

    public static Integer defaultToZero(Integer value) {
        return Optional.ofNullable(value).orElse(0);
    }

    public static Long defaultToZero(Long value) {
        return Optional.ofNullable(value).orElse(0L);
    }

    public static BigDecimal toRndBigDecimal(Num num) {
        if (num instanceof NaN) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(num.floatValue()).round(new MathContext(4, RoundingMode.CEILING));
    }

    public static BigDecimal toRndBigDecimal(BigDecimal num) {
        return BigDecimal.valueOf(num.floatValue()).round(new MathContext(4, RoundingMode.CEILING));
    }

    public static BigDecimal toBigDecimal(String value) {
        value = normalizeNumber(value);
        if (StringUtils.isEmpty(value)) {
            return BigDecimal.ZERO;
        }

        try {
            return BigDecimal.valueOf(Double.parseDouble(value));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert value '" + value + "' to BigDecimal", e);
        }
    }

    public static DecimalNum toDecimalNum(BigDecimal value) {
        return DecimalNum.valueOf(value);
    }

    public static BigDecimal toNegative(BigDecimal value) {
        return value.doubleValue() > 0 ? value.negate() : value;
    }

    //----------------------------------------------------
    //------------------Private methods-------------------
    //----------------------------------------------------
    private static String normalizeNumber(String value) {
        return value.toLowerCase().replaceAll("[^0-9.]+", "");
    }

}
