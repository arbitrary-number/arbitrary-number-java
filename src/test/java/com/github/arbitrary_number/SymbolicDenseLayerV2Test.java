package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolicDenseLayerV2Test {

    @Test
    public void testForwardWithSigmoidAndDerivatives() {
        // Input variable names
        List<String> inputNames = List.of("x1", "x2");

        // Create weights as SymbolicExpression terms with constant coefficients
        // For simplicity: weights = [[0.5, -1.0], [1.5, 2.0]]
        SymbolicExpression w00 = SymbolicExpression.term(BigInteger.valueOf(5), BigInteger.ONE, BigInteger.TEN);   // 0.5 = 5/10
        SymbolicExpression w01 = SymbolicExpression.term(BigInteger.valueOf(-1), BigInteger.ONE, BigInteger.ONE);   // -1.0
        SymbolicExpression w10 = SymbolicExpression.term(BigInteger.valueOf(15), BigInteger.ONE, BigInteger.TEN);  // 1.5 = 15/10
        SymbolicExpression w11 = SymbolicExpression.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE);    // 2.0

        SymbolicExpression[][] weights = {
            {w00, w01},
            {w10, w11}
        };

        // Biases = [0.1, -0.2]
        SymbolicExpression b0 = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.TEN);    // 0.1 = 1/10
        SymbolicExpression b1 = SymbolicExpression.term(BigInteger.valueOf(-2), BigInteger.ONE, BigInteger.TEN); // -0.2 = -2/10

        List<SymbolicExpression> biases = List.of(b0, b1);

        // Create layer
        SymbolicDenseLayerV2 layer = new SymbolicDenseLayerV2(inputNames, weights, biases);

        // Forward with sigmoid activation
        List<SymbolicExpression> outputs = layer.forwardWithActivation(SymbolicDenseLayerV2::sigmoid);

        // Differentiate outputs w.r.t. inputs "x1" and "x2"
        List<SymbolicExpression> dOut_dx1 = outputs.stream()
            .map(output -> output.differentiate("x1"))
            .toList();

        List<SymbolicExpression> dOut_dx2 = outputs.stream()
            .map(output -> output.differentiate("x2"))
            .toList();

        // Sample numeric inputs to evaluate expressions
        var sampleInputs = new java.util.HashMap<String, Double>();
        sampleInputs.put("x1", 1.0);
        sampleInputs.put("x2", -1.0);

        // Evaluate outputs and derivatives numerically
        for (int i = 0; i < outputs.size(); i++) {
            double outVal = outputs.get(i).evaluate(sampleInputs);
            double dVal_dx1 = dOut_dx1.get(i).evaluate(sampleInputs);
            double dVal_dx2 = dOut_dx2.get(i).evaluate(sampleInputs);

            System.out.println("Output " + i + " = " + outVal);
            System.out.println("dOutput/dx1 " + i + " = " + dVal_dx1);
            System.out.println("dOutput/dx2 " + i + " = " + dVal_dx2);

            // Check output in [0, 1] due to sigmoid
            assertTrue(outVal >= 0 && outVal <= 1, "Output should be in [0,1]");

            // Derivatives of sigmoid layer should be non-negative (rough check)
            assertTrue(dVal_dx1 >= 0, "Derivative wrt x1 should be >= 0");
            assertTrue(dVal_dx2 >= 0, "Derivative wrt x2 should be >= 0");
        }
    }
}
