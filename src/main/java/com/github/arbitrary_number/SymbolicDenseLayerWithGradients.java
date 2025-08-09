package com.github.arbitrary_number;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SymbolicDenseLayerWithGradients {

    private final ArbitraryNumberV2[][] weights;
    private final ArbitraryNumberV2[] biases;

    public SymbolicDenseLayerWithGradients(ArbitraryNumberV2[][] weights, ArbitraryNumberV2[] biases) {
        this.weights = weights;
        this.biases = biases;
    }

    public Result forwardWithGradients(ArbitraryNumberV2[] inputs) {
        int outputSize = weights.length;
        ArbitraryNumberV2[] outputs = new ArbitraryNumberV2[outputSize];
        ArbitraryNumberV2[][] inputGradients = new ArbitraryNumberV2[outputSize][inputs.length];

        for (int i = 0; i < outputSize; i++) {
            ArbitraryNumberV2 sum = biases[i];
            for (int j = 0; j < inputs.length; j++) {
                ArbitraryNumberV2 product = ArbitraryNumberV2.multiply(weights[i][j], inputs[j]);
                sum = ArbitraryNumberV2.add(sum, product);

                // Gradient of output[i] w.r.t input[j] is weight[i][j]
                inputGradients[i][j] = weights[i][j];
            }
            outputs[i] = sum;
        }

        return new Result(outputs, inputGradients);
    }

    public static class Result {
        public final ArbitraryNumberV2[] outputs;
        public final ArbitraryNumberV2[][] inputGradients;

        public Result(ArbitraryNumberV2[] outputs, ArbitraryNumberV2[][] inputGradients) {
            this.outputs = outputs;
            this.inputGradients = inputGradients;
        }
    }
}
