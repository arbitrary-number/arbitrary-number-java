package com.github.arbitrary_number;

import java.math.BigInteger;

public class ConstantNode implements SymbolicNode {
    private final BigInteger value;

    public ConstantNode(BigInteger value) {
        this.value = value;
    }

    @Override
    public SymbolicExpression forward() {
        return SymbolicExpression.term(value, BigInteger.ONE, BigInteger.ONE);
    }

    @Override
    public SymbolicExpression backward(String variableName) {
        return SymbolicExpression.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
    }
}
