package com.github.arbitrary_number;

import java.math.BigInteger;

public class DemoV2Extended {
    public static void main(String[] args) {
        ArbitraryNumberV2 half = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2)); // 1/2
        ArbitraryNumberV2 three = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(3), BigInteger.ONE); // 3

        ArbitraryNumberV2 negHalf = ArbitraryNumberV2.negate(half);
        ArbitraryNumberV2 powerExample = ArbitraryNumberV2.power(half, three); // (1/2)^3 = 1/8

        ArbitraryNumberV2 logExample = ArbitraryNumberV2.log(three, ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(9), BigInteger.ONE)); // log_3(9) = 2

        System.out.println("Negation: " + negHalf + " = " + negHalf.evaluate(30));
        System.out.println("Power: " + powerExample + " = " + powerExample.evaluate(30));
        System.out.println("Logarithm: " + logExample + " = " + logExample.evaluate(30));
    }
}
