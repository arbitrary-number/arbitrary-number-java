package com.github.arbitrary_number;

import java.math.BigInteger;

public class CollatzTest {

    public static boolean symbolicCollatz(long startValue, long maxSteps) {
        ArbitraryNumber n = new ArbitraryNumber();
        n.addTerm(1, startValue, 1);

        for (long step = 0; step < maxSteps; step++) {
            // If n == 1 exactly, done
            if (n.getTerms().size() == 1) {
                ArbitraryNumber.Term t = n.getTerms().get(0);
                if (t.c.equals(BigInteger.ONE) && t.a.equals(BigInteger.ONE) && t.b.equals(BigInteger.ONE)) {
                    return true;
                }
            }

            // Check parity via numerator
            ArbitraryNumber.Term t = n.getTerms().get(0);
            boolean isEven = t.a.mod(BigInteger.TWO).equals(BigInteger.ZERO);

            if (isEven) {
                // n = n / 2
                ArbitraryNumber two = new ArbitraryNumber();
                two.addTerm(1, 2, 1);
                n = n.divideBy(two);
            } else {
                // n = 3n + 1
                ArbitraryNumber three = new ArbitraryNumber();
                three.addTerm(3, 1, 1);
                n = n.multiply(three);

                ArbitraryNumber one = new ArbitraryNumber();
                one.addTerm(1, 1, 1);
                n = n.add(one);
            }
        }

        return false; // Did not converge in time
    }

    public static void main(String[] args) {
        long[] testValues = {3, 7, 27, 97, 871, 6171, 77031}; // Known tricky ones
        long maxSteps = 10000;

        System.out.println("Symbolic Collatz Convergence Test:");
        for (long start : testValues) {
            boolean converged = symbolicCollatz(start, maxSteps);
            System.out.printf("  Start = %-6d => %s\n", start, converged ? "Converged ✅" : "Failed ❌");
        }
    }
}
