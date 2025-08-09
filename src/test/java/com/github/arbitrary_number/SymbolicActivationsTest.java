package com.github.arbitrary_number;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class SymbolicActivationsTest {

    // Substitute variable with numeric value recursively, then simplify
    private SymbolicExpression substituteVariable(SymbolicExpression expr, String varName, double val) {
        switch (expr.op) {
            case VARIABLE -> {
                if (expr.variableName.equals(varName)) {
                    BigDecimal bdVal = BigDecimal.valueOf(val);
                    int scale = bdVal.scale();
                    java.math.BigInteger numerator = bdVal.movePointRight(scale).toBigIntegerExact();
                    java.math.BigInteger denominator = java.math.BigInteger.TEN.pow(scale);
                    return SymbolicExpression.term(java.math.BigInteger.ONE, numerator, denominator);
                } else {
                    return expr;
                }
            }
            case TERM -> {
            	return expr;
            }
            default -> {
                SymbolicExpression[] newChildren = new SymbolicExpression[expr.children.size()];
                for (int i = 0; i < expr.children.size(); i++) {
                    newChildren[i] = substituteVariable(expr.children.get(i), varName, val);
                }
                return SymbolicExpression.node(expr.op, newChildren).simplify();
            }
        }
    }

    @Test
    public void testSigmoidAndDerivative() {
        SymbolicExpression x = SymbolicExpression.variable("x");
        SymbolicExpression sig = SymbolicActivations.sigmoid(x);
        SymbolicExpression grad = sig.differentiate("x").simplify();

        double[] testVals = {-5.0, -1.0, 0.0, 1.0, 5.0};
        for (double val : testVals) {
            SymbolicExpression substitutedSig = substituteVariable(sig, "x", val);
            SymbolicExpression substitutedGrad = substituteVariable(grad, "x", val);

            BigDecimal sigEval = substitutedSig.evaluate(40);
            BigDecimal gradEval = substitutedGrad.evaluate(40);

            System.out.println("x = " + val);
            System.out.println("Sigmoid(x) = " + sigEval.toPlainString());
            System.out.println("Sigmoid'(x) = " + gradEval.toPlainString());
            System.out.println();

            // Sigmoid derivative should be positive for all real x
            assertTrue(gradEval.compareTo(BigDecimal.ZERO) > 0,
                    "Expected sigmoid derivative > 0 at x=" + val);
        }
    }
}
