package com.github.arbitrary_number;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class SymbolicExpressionTest {

    @Test
    public void testSymbolicGradientOfRationalFunction() {
        // Define variable x
        SymbolicExpression x = SymbolicExpression.variable("x");

        // Numerator: x^2 + 3x + 2
        SymbolicExpression xSquared = SymbolicExpression.power(x, SymbolicExpression.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE));
        SymbolicExpression threeX = SymbolicExpression.multiply(
                SymbolicExpression.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE),
                x
        );
        SymbolicExpression two = SymbolicExpression.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression numerator = SymbolicExpression.add(SymbolicExpression.add(xSquared, threeX), two);

        // Denominator: x + 1
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression denominator = SymbolicExpression.add(x, one);

        // f(x) = (x^2 + 3x + 2) / (x + 1)
        SymbolicExpression fx = SymbolicExpression.divide(numerator, denominator);

        // Compute gradient
        SymbolicExpression grad = fx.symbolicGrad(x).simplify();

        // Expected result: 1
        SymbolicExpression expected = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        // Compare simplified gradient to expected
        assertEquals(expected, grad, "Gradient should be 1");
    }

    @Test
    public void testCrossEntropyLossEvaluation() {
        // y = 1
        SymbolicExpression y = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        // p = 0.8 = 4/5
        SymbolicExpression p = SymbolicExpression.term(BigInteger.ONE, BigInteger.valueOf(4), BigInteger.valueOf(5));

        // (1 - y)
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression oneMinusY = SymbolicExpression.subtract(one, y);

        // (1 - p)
        SymbolicExpression oneMinusP = SymbolicExpression.subtract(one, p);

        // y * log(p)
        SymbolicExpression yLogP = SymbolicExpression.multiply(y, SymbolicExpression.log(p));

        // (1 - y) * log(1 - p)
        SymbolicExpression zeroLog = SymbolicExpression.multiply(oneMinusY, SymbolicExpression.log(oneMinusP));

        // y * log(p) + (1 - y) * log(1 - p)
        SymbolicExpression sum = SymbolicExpression.add(yLogP, zeroLog);

        // -[...]
        SymbolicExpression negated = SymbolicExpression.negate(sum);

        // Evaluate the loss
        BigDecimal result = negated.evaluate(20);

        // Expected: -log(0.8) ≈ 0.22314355
        BigDecimal expected = new BigDecimal("0.2231435513142097");

        // Assert result is close
        assertEquals(0, result.subtract(expected).abs().compareTo(new BigDecimal("0.00000001")),
            "Cross-entropy loss should match expected value");
    }

}
