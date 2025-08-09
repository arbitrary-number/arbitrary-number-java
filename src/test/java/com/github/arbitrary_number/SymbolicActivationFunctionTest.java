package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolicActivationFunctionTest {

    /**
     * Computes sigmoid(x) = 1 / (1 + e^(-x)) symbolically using ArbitraryNumberV2.
     */
    private ArbitraryNumberV2 sigmoid(ArbitraryNumberV2 x) {
        // Compute e^(-x)
        ArbitraryNumberV2 negX = ArbitraryNumberV2.multiply(
            ArbitraryNumberV2.term(BigInteger.valueOf(-1), BigInteger.ONE, BigInteger.ONE),
            x
        );
        ArbitraryNumberV2 expNegX = ArbitraryNumberV2.exp(negX);

        // denom = 1 + e^(-x)
        ArbitraryNumberV2 denom = ArbitraryNumberV2.add(
            ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            expNegX
        );

        // sigmoid = 1 / denom
        return ArbitraryNumberV2.divide(
            ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            denom
        );
    }

    @Test
    public void testSymbolicSigmoid() {
        // Example input: x = 2 (symbolic term 2/1)
        ArbitraryNumberV2 x = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.ONE);

        ArbitraryNumberV2 sig = sigmoid(x);

        // Print symbolic expression for manual inspection
        System.out.println("Symbolic Sigmoid(2): " + sig);

        // Evaluate sigmoid(2) to 40 decimal places
        var eval = sig.evaluate(40);
        System.out.println("Evaluated Sigmoid(2) (prec=40): " + eval);

        // Sigmoid(2) ≈ 0.88079707797788238397...
        double expected = 1.0 / (1.0 + Math.exp(-2.0));
        double actual = eval.doubleValue();

        // Assert approximate equality within reasonable tolerance
        assertEquals(expected, actual, 1e-15, "Sigmoid evaluated value should match Math.exp based double");
    }
}
