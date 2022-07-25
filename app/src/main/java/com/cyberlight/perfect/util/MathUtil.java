package com.cyberlight.perfect.util;

public class MathUtil {
    /**
     * 辗转相除法求最大公约数
     *
     * @param a 一个数
     * @param b 另一个数
     * @return 两数的最大公约数
     */
    public static long computeGCD(long a, long b) {
        long r;
        while (true) {
            if (a > b) {
                // 取余运算符要求符号两边都是整型数
                r = a % b;
                if (r != 0)
                    a = r;
                else
                    break;
            } else {
                r = b % a;
                if (r != 0)
                    b = r;
                else
                    break;
            }
        }
        return Math.min(a, b);
    }
}
