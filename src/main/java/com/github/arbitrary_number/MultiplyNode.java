package com.github.arbitrary_number;

public class MultiplyNode implements SymbolicNode {
    private final SymbolicNode left;
    private final SymbolicNode right;

    public MultiplyNode(SymbolicNode left, SymbolicNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public SymbolicExpression forward() {
        return SymbolicExpression.multiply(left.forward(), right.forward());
    }

    @Override
    public SymbolicExpression backward(String variableName) {
        // product rule: u'v + uv'
        SymbolicExpression u = left.forward();
        SymbolicExpression v = right.forward();
        SymbolicExpression uPrime = left.backward(variableName);
        SymbolicExpression vPrime = right.backward(variableName);

        return SymbolicExpression.add(
            SymbolicExpression.multiply(uPrime, v),
            SymbolicExpression.multiply(u, vPrime));
    }
}
