package com.github.arbitrary_number;

public class AddNode implements SymbolicNode {
    private final SymbolicNode left;
    private final SymbolicNode right;

    public AddNode(SymbolicNode left, SymbolicNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public SymbolicExpression forward() {
        return SymbolicExpression.add(left.forward(), right.forward());
    }

    @Override
    public SymbolicExpression backward(String variableName) {
        return SymbolicExpression.add(left.backward(variableName), right.backward(variableName));
    }
}
