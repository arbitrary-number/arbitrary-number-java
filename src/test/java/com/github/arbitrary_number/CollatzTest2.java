package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CollatzTest2 {

    // Helper: isEven
    private boolean isEven(SymbolicExpression n) {
        return n.numerator.mod(BigInteger.TWO).equals(BigInteger.ZERO);
    }

    // Compute next Collatz step for term only
    private SymbolicExpression collatzStep(SymbolicExpression n) {
        if (n.op != SymbolicExpression.Operation.TERM) {
            throw new IllegalArgumentException("collatzStep only supports TERM nodes");
        }
        BigInteger val = n.coefficient.multiply(n.numerator).divide(n.denominator);
        if (val.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            // even: n / 2
            return SymbolicExpression.term(BigInteger.ONE, val.divide(BigInteger.TWO), BigInteger.ONE);
        } else {
            // odd: 3n + 1
            BigInteger threeNPlusOne = val.multiply(BigInteger.valueOf(3)).add(BigInteger.ONE);
            return SymbolicExpression.term(BigInteger.ONE, threeNPlusOne, BigInteger.ONE);
        }
    }

    @Test
    public void testCollatzSequence() {
        SymbolicExpression start = SymbolicExpression.term(BigInteger.ONE, BigInteger.valueOf(7), BigInteger.ONE);

        // Expected Collatz sequence starting at 7: 7, 22, 11, 34, 17, 52, 26, 13, 40, 20, 10, 5, 16, 8, 4, 2, 1
        BigInteger[] expected = {
            BigInteger.valueOf(7), BigInteger.valueOf(22), BigInteger.valueOf(11), BigInteger.valueOf(34),
            BigInteger.valueOf(17), BigInteger.valueOf(52), BigInteger.valueOf(26), BigInteger.valueOf(13),
            BigInteger.valueOf(40), BigInteger.valueOf(20), BigInteger.valueOf(10), BigInteger.valueOf(5),
            BigInteger.valueOf(16), BigInteger.valueOf(8), BigInteger.valueOf(4), BigInteger.valueOf(2), BigInteger.ONE
        };

        SymbolicExpression current = start;
        for (BigInteger val : expected) {
            BigInteger currentVal = current.coefficient.multiply(current.numerator).divide(current.denominator);
            assertEquals(val, currentVal, "Collatz step value mismatch");
            if (val.equals(BigInteger.ONE)) break;
            current = collatzStep(current);
        }
    }

    @Test
    public void testSymbolicCollatzStep() {
        // Variable n
        SymbolicExpression n = SymbolicExpression.variable("n");

        // Expression for Collatz step (symbolic): if n even -> n/2 else 3n + 1
        // We'll just build the two branches symbolically

        // n / 2 (exact fraction)
        SymbolicExpression half = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2));
        SymbolicExpression nDiv2 = SymbolicExpression.multiply(n, half);

        // 3n + 1
        SymbolicExpression three = SymbolicExpression.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression threeN = SymbolicExpression.multiply(three, n);
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression threeNplus1 = SymbolicExpression.add(threeN, one);

        // Output expressions for each branch
        System.out.println("Collatz step if even: " + nDiv2);
        System.out.println("Collatz step if odd: " + threeNplus1);

        // Now build an expression for symbolic iteration: f(n) = 3n + 1 (odd)
        // Next step: f(f(n))
        SymbolicExpression secondStep = threeNplus1;  // just alias to simplify

        // Test symbolic differentiation on 3n + 1 (should be 3)
        SymbolicExpression derivative = threeNplus1.differentiate("n");
        System.out.println("Derivative of 3n + 1 w.r.t n: " + derivative);

        SymbolicExpression result = derivative.simplify();

        assertEquals(SymbolicExpression.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE), result);
    }

}

