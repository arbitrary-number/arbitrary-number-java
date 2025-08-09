package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CollatzAdvancedTest2 {

    // Collatz step symbolically: if even n -> n/2, if odd n -> 3n + 1
    // Here we symbolically sum both branches to keep algebraic form (approximation)
    private ArbitraryNumberV2 collatzStep(ArbitraryNumberV2 n) {
        ArbitraryNumberV2 half = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2));
        ArbitraryNumberV2 three = ArbitraryNumberV2.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 one = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        ArbitraryNumberV2 evenPart = ArbitraryNumberV2.multiply(n, half);
        ArbitraryNumberV2 oddPart = ArbitraryNumberV2.add(ArbitraryNumberV2.multiply(three, n), one);

        // Sum both (not a proper conditional, but symbolic approx)
        return ArbitraryNumberV2.add(evenPart, oddPart);
    }

    @Test
    public void testMultiStepCollatzWithDerivatives() {
        ArbitraryNumberV2 n = ArbitraryNumberV2.variable("n");
        ArbitraryNumberV2 current = n;

        int steps = 7;  // Push it higher if you want
        for (int i = 1; i <= steps; i++) {
            current = collatzStep(current);
            ArbitraryNumberV2 derivative = current.differentiate("n");
            System.out.println("Step " + i + " expression: " + current);
            System.out.println("Step " + i + " derivative: " + derivative);
            assertNotNull(current);
            assertNotNull(derivative);
        }
    }
}

