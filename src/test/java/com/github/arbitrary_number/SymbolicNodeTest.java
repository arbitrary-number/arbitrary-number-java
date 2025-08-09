package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SymbolicNodeTest {

    @Test
    public void testSigmoidAndDerivative() {
        VariableNode x = new VariableNode("x");
        SigmoidNode sigmoid = new SigmoidNode(x);

        SymbolicExpression sigmoidExpr = sigmoid.forward();
        SymbolicExpression derivativeExpr = sigmoid.backward("x");

        double[] testPoints = {-5.0, -1.0, 0.0, 1.0, 5.0};
        int precision = 30;

        for (double val : testPoints) {
            Map<String, BigDecimal> input = new HashMap<>();
            input.put("x", BigDecimal.valueOf(val));

            BigDecimal sigmoidVal = SymbolicNode.evaluateSymbolicExpression(sigmoidExpr, input, precision);
            BigDecimal derivativeVal = SymbolicNode.evaluateSymbolicExpression(derivativeExpr, input, precision);

            // Expected sigmoid formula
            double expectedSigmoid = 1.0 / (1.0 + Math.exp(-val));
            double expectedDerivative = expectedSigmoid * (1.0 - expectedSigmoid);

            System.out.println("x = " + val);
            System.out.println("Sigmoid(x) = " + sigmoidVal);
            System.out.println("Sigmoid'(x) = " + derivativeVal);

            assertEquals(expectedSigmoid, sigmoidVal.doubleValue(), 1e-6, "Sigmoid mismatch at x=" + val);
            assertEquals(expectedDerivative, derivativeVal.doubleValue(), 1e-6, "Sigmoid derivative mismatch at x=" + val);
        }
    }
}
