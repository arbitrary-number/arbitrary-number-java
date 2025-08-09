package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollatzAdvancedTest {

    private static ArbitraryNumberV2 collatzStep(ArbitraryNumberV2 n) {
        // Collatz step:
        // if even: n / 2
        // if odd: 3*n + 1
        // Here, to keep it symbolic, we'll represent the function piecewise by returning
        // an expression that depends on n, leaving the 'if' logic symbolic (not a boolean branch).
        // For testing, just build the "odd" branch formula for demonstration, since "if" is not symbolic in your class.

        // even branch: n * (1/2)
        ArbitraryNumberV2 evenBranch = ArbitraryNumberV2.multiply(n, ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2)));

        // odd branch: (3 * n) + 1
        ArbitraryNumberV2 three = ArbitraryNumberV2.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 oddBranch = ArbitraryNumberV2.add(ArbitraryNumberV2.multiply(three, n), ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE));

        // Return both branches as a symbolic "choice" (here just sum for demonstration)
        // (In real math, this is a piecewise function, but your class can't express 'if' yet.)
        return ArbitraryNumberV2.add(evenBranch, oddBranch);
    }

    @Test
    public void testTwoStepCollatzSymbolic() {
        ArbitraryNumberV2 n = ArbitraryNumberV2.variable("n");

        // First step: Collatz step on n
        ArbitraryNumberV2 firstStep = collatzStep(n);

        // Second step: Collatz step on result of first step
        ArbitraryNumberV2 secondStep = collatzStep(firstStep);

        System.out.println("Two-step Collatz expression:");
        System.out.println(secondStep);

        // Since this is a sum of branches, expected to be complex. Just check structure non-null and ops.
        assertEquals(ArbitraryNumberV2.Operation.ADD, secondStep.op);
        assertEquals(2, secondStep.children.size());

        // Could also check derivative structure for demonstration
        ArbitraryNumberV2 derivative = secondStep.differentiate("n");
        System.out.println("Derivative of two-step Collatz expression:");
        System.out.println(derivative);

        // The derivative should not be trivial zero or one — just check type
        assertEquals(ArbitraryNumberV2.Operation.ADD, derivative.op);
    }
}
