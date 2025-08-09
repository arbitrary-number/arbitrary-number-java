package com.github.arbitrary_number;

import java.math.BigInteger;

public class VariableNode implements SymbolicNode {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public SymbolicExpression forward() {
        return SymbolicExpression.variable(name);
    }

    @Override
    public SymbolicExpression backward(String variableName) {
        if (name.equals(variableName)) {
            return SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        }
        return SymbolicExpression.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
    }
}
