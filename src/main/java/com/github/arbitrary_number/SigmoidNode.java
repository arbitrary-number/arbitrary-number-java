package com.github.arbitrary_number;

import java.math.BigInteger;

public class SigmoidNode implements SymbolicNode {
    private final SymbolicNode input;

    public SigmoidNode(SymbolicNode input) {
        this.input = input;
    }

    @Override
    public SymbolicExpression forward() {
        // sigmoid(x) = 1 / (1 + exp(-x))
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression negX = SymbolicExpression.negate(input.forward());

        // exp(-x) = e^(-x), e approximated by base exp constant or just use power with e constant base
        // We'll use Math.E constant as base (approximation) in power for now
        BigInteger eNumerator = BigInteger.valueOf(2718281828459045235L);
        BigInteger eDenominator = BigInteger.valueOf(1000000000000000000L);
        SymbolicExpression eConst = SymbolicExpression.term(BigInteger.ONE, eNumerator, eDenominator);

        SymbolicExpression expNegX = SymbolicExpression.power(eConst, negX);

        SymbolicExpression denom = SymbolicExpression.add(one, expNegX);

        return SymbolicExpression.divide(one, denom);
    }

    @Override
    public SymbolicExpression backward(String variableName) {
        SymbolicExpression f = forward(); // sigmoid(x)
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

        // derivative of sigmoid: f * (1 - f) * dx/dvar
        SymbolicExpression df_dx = SymbolicExpression.multiply(f, SymbolicExpression.subtract(one, f));
        SymbolicExpression dInput = input.backward(variableName);

        return SymbolicExpression.multiply(df_dx, dInput);
    }
}
