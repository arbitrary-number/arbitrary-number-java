package com.github.arbitrary_number;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class ArbitraryNumberV2Test {

    @Test
    public void testSymbolicGradientOfRationalFunction() {
        // Define variable x
        ArbitraryNumberV2 x = ArbitraryNumberV2.variable("x");

        // Numerator: x^2 + 3x + 2
        ArbitraryNumberV2 xSquared = ArbitraryNumberV2.power(x, ArbitraryNumberV2.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE));
        ArbitraryNumberV2 threeX = ArbitraryNumberV2.multiply(
                ArbitraryNumberV2.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE),
                x
        );
        ArbitraryNumberV2 two = ArbitraryNumberV2.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 numerator = ArbitraryNumberV2.add(ArbitraryNumberV2.add(xSquared, threeX), two);

        // Denominator: x + 1
        ArbitraryNumberV2 one = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 denominator = ArbitraryNumberV2.add(x, one);

        // f(x) = (x^2 + 3x + 2) / (x + 1)
        ArbitraryNumberV2 fx = ArbitraryNumberV2.divide(numerator, denominator);

        // Compute gradient
        ArbitraryNumberV2 grad = fx.symbolicGrad(x).simplify();

        // Expected result: 1
        ArbitraryNumberV2 expected = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        // Compare simplified gradient to expected
        assertEquals(expected, grad, "Gradient should be 1");
    }

    @Test
    public void testCrossEntropyLossEvaluation() {
        // y = 1
        ArbitraryNumberV2 y = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        // p = 0.8 = 4/5
        ArbitraryNumberV2 p = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(4), BigInteger.valueOf(5));

        // (1 - y)
        ArbitraryNumberV2 one = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 oneMinusY = ArbitraryNumberV2.subtract(one, y);

        // (1 - p)
        ArbitraryNumberV2 oneMinusP = ArbitraryNumberV2.subtract(one, p);

        // y * log(p)
        ArbitraryNumberV2 yLogP = ArbitraryNumberV2.multiply(y, ArbitraryNumberV2.log(p));

        // (1 - y) * log(1 - p)
        ArbitraryNumberV2 zeroLog = ArbitraryNumberV2.multiply(oneMinusY, ArbitraryNumberV2.log(oneMinusP));

        // y * log(p) + (1 - y) * log(1 - p)
        ArbitraryNumberV2 sum = ArbitraryNumberV2.add(yLogP, zeroLog);

        // -[...]
        ArbitraryNumberV2 negated = ArbitraryNumberV2.negate(sum);

        // Evaluate the loss
        BigDecimal result = negated.evaluate(20);

        // Expected: -log(0.8) ≈ 0.22314355
        BigDecimal expected = new BigDecimal("0.2231435513142097");

        // Assert result is close
        assertEquals(0, result.subtract(expected).abs().compareTo(new BigDecimal("0.00000001")),
            "Cross-entropy loss should match expected value");
    }

}
