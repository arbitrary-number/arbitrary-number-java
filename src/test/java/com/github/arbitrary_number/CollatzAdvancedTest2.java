package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CollatzAdvancedTest2 {

    // Collatz step symbolically: if even n -> n/2, if odd n -> 3n + 1
    // Here we symbolically sum both branches to keep algebraic form (approximation)
    private SymbolicExpression collatzStep(SymbolicExpression n) {
        SymbolicExpression half = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2));
        SymbolicExpression three = SymbolicExpression.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        SymbolicExpression evenPart = SymbolicExpression.multiply(n, half);
        SymbolicExpression oddPart = SymbolicExpression.add(SymbolicExpression.multiply(three, n), one);

        // Sum both (not a proper conditional, but symbolic approx)
        return SymbolicExpression.add(evenPart, oddPart);
    }

    @Test
    public void testMultiStepCollatzWithDerivatives() {
        SymbolicExpression n = SymbolicExpression.variable("n");
        SymbolicExpression current = n;

        int steps = 7;  // Push it higher if you want
        for (int i = 1; i <= steps; i++) {
            current = collatzStep(current);
            SymbolicExpression derivative = current.differentiate("n");
            System.out.println("Step " + i + " expression: " + current);
            System.out.println("Step " + i + " derivative: " + derivative);
            assertNotNull(current);
            assertNotNull(derivative);
        }
    }
}

