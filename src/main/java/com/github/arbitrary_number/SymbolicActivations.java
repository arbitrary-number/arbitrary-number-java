package com.github.arbitrary_number;

import java.math.BigInteger;

public class SymbolicActivations {

    // Constant e ≈ 2.71828 as a SymbolicExpression term (can be improved to more precise or symbolic e)
    private static final SymbolicExpression E = SymbolicExpression.term(
            BigInteger.ONE,
            BigInteger.valueOf(2718281828459045235L), // numerator 2.718281828459045235...
            BigInteger.valueOf(1000000000000000000L)); // denominator 1e18

    private static final SymbolicExpression ONE = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

    /**
     * Symbolic exponential e^x using power with base e.
     */
    public static SymbolicExpression exp(SymbolicExpression x) {
        return SymbolicExpression.power(E, x);
    }

    /**
     * Symbolic sigmoid function: 1 / (1 + e^{-x})
     */
//    public static SymbolicExpression sigmoid(SymbolicExpression x) {
//        SymbolicExpression negX = SymbolicExpression.negate(x);
//        SymbolicExpression expNegX = exp(negX);
//        SymbolicExpression denom = SymbolicExpression.add(ONE, expNegX);
//        return SymbolicExpression.divide(ONE, denom);
//    }

    public static SymbolicExpression sigmoid(SymbolicExpression x) {
        SymbolicExpression one = SymbolicExpression.term(
                java.math.BigInteger.ONE,
                java.math.BigInteger.ONE,
                java.math.BigInteger.ONE);

        SymbolicExpression negX = SymbolicExpression.negate(x);

        SymbolicExpression eBase = SymbolicExpression.term(
            new java.math.BigInteger("2718281828459045235"),
            java.math.BigInteger.ONE,
            new java.math.BigInteger("1000000000000000000")
        );

        SymbolicExpression expNegX = SymbolicExpression.power(eBase, negX);

        SymbolicExpression denom = SymbolicExpression.add(one, expNegX);

        return SymbolicExpression.divide(one, denom);
    }

    /**
     * Symbolic tanh function: (e^{x} - e^{-x}) / (e^{x} + e^{-x})
     */
    public static SymbolicExpression tanh(SymbolicExpression x) {
        SymbolicExpression expX = exp(x);
        SymbolicExpression expNegX = exp(SymbolicExpression.negate(x));
        SymbolicExpression numerator = SymbolicExpression.subtract(expX, expNegX);
        SymbolicExpression denominator = SymbolicExpression.add(expX, expNegX);
        return SymbolicExpression.divide(numerator, denominator);
    }

    /**
     * Symbolic softplus function: log(1 + e^x)
     */
    public static SymbolicExpression softplus(SymbolicExpression x) {
        SymbolicExpression expX = exp(x);
        SymbolicExpression insideLog = SymbolicExpression.add(ONE, expX);
        return SymbolicExpression.log(insideLog);
    }
}
