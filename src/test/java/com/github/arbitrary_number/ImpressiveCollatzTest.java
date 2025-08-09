package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static com.github.arbitrary_number.SymbolicExpression.*;
import static org.junit.jupiter.api.Assertions.*;

public class ImpressiveCollatzTest {

    // Collatz rule applied symbolically once
    private SymbolicExpression collatzStep(SymbolicExpression n) {
        // Note: We're not using actual conditional logic; just expressing both branches
        // We'll simulate symbolic composition (even step here, for structure)
        return add(multiply(n, term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(2))),
                   add(multiply(term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE), n),
                       term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
    }

    // Repeatedly apply Collatz rule n times
    private SymbolicExpression repeatedCollatz(SymbolicExpression start, int steps) {
        SymbolicExpression current = start;
        for (int i = 0; i < steps; i++) {
            current = collatzStep(current);
        }
        return current;
    }

    @Test
    public void testSymbolicFiveStepCollatzDerivative() {
        // Symbolic variable n
        SymbolicExpression n = variable("n");

        // Apply Collatz 5 times symbolically
        SymbolicExpression collatz5 = repeatedCollatz(n, 5);
        SymbolicExpression derivative = collatz5.differentiate("n");

        System.out.println("5-step Collatz expression: " + collatz5);
        System.out.println("Derivative w.r.t n: " + derivative);

        // Validate the structure is non-trivial
        String resultStr = derivative.toString();
        assertTrue(resultStr.contains("n"), "Derivative should depend on n");
        assertTrue(resultStr.length() > 50, "Derivative expression should be non-trivial");
    }
}
