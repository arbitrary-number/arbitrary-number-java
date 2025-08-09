package com.github.arbitrary_number;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SymbolicDenseLayerV2 {

    private final List<SymbolicExpression> inputVars;
    private final SymbolicExpression[][] weights; // shape: [outputSize][inputSize]
    private final List<SymbolicExpression> biases; // length = outputSize

    public SymbolicDenseLayerV2(List<String> inputVariableNames,
                                SymbolicExpression[][] weights,
                                List<SymbolicExpression> biases) {
        // Create symbolic variables for inputs from names
        inputVars = new ArrayList<>();
        for (String varName : inputVariableNames) {
            inputVars.add(SymbolicExpression.variable(varName));
        }

        this.weights = weights;
        this.biases = biases;
    }

    // Compute layer outputs symbolically without activation
    public List<SymbolicExpression> forward() {
        int outputSize = weights.length;
        int inputSize = inputVars.size();

        List<SymbolicExpression> outputs = new ArrayList<>();

        for (int j = 0; j < outputSize; j++) {
            SymbolicExpression sum = SymbolicExpression.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE); // zero term
            for (int i = 0; i < inputSize; i++) {
                SymbolicExpression weightedInput = SymbolicExpression.multiply(weights[j][i], inputVars.get(i));
                sum = SymbolicExpression.add(sum, weightedInput);
            }
            // Add bias
            sum = SymbolicExpression.add(sum, biases.get(j));
            outputs.add(sum);
        }
        return outputs;
    }

    // Compute layer outputs symbolically with given activation function
    // activationFunction accepts SymbolicExpression and returns SymbolicExpression (e.g. sigmoid)
    public List<SymbolicExpression> forwardWithActivation(SymbolicActivation activationFunction) {
        List<SymbolicExpression> preActivations = forward();
        List<SymbolicExpression> activatedOutputs = new ArrayList<>();
        for (SymbolicExpression z : preActivations) {
            activatedOutputs.add(activationFunction.apply(z));
        }
        return activatedOutputs;
    }

    // Functional interface for symbolic activation functions
    @FunctionalInterface
    public interface SymbolicActivation {
        SymbolicExpression apply(SymbolicExpression input);
    }

    // Example sigmoid implementation
    public static SymbolicExpression sigmoid(SymbolicExpression x) {
        // sigmoid(x) = 1 / (1 + e^-x) = 1 / (1 + exp(-x))
        SymbolicExpression one = SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
        SymbolicExpression negX = SymbolicExpression.negate(x);
        SymbolicExpression expNegX = SymbolicExpression.power(
            SymbolicExpression.term(BigInteger.valueOf(2718281828459045235L), BigInteger.ONE, BigInteger.valueOf(1000000000000000000L)), // e approx 2.71828
            negX
        );
        SymbolicExpression denom = SymbolicExpression.add(one, expNegX);
        return SymbolicExpression.divide(one, denom);
    }
}
