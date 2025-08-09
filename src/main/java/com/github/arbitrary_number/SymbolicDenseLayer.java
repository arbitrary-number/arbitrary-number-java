package com.github.arbitrary_number;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SymbolicDenseLayer {

    private final int inputSize;
    private final int outputSize;
    private final ArbitraryNumberV2[][] weights;
    private final ArbitraryNumberV2[] biases;

    public SymbolicDenseLayer(int inputSize, int outputSize) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.weights = new ArbitraryNumberV2[outputSize][inputSize];
        this.biases = new ArbitraryNumberV2[outputSize];

        initializeSymbolicWeights();
    }

    private void initializeSymbolicWeights() {
        Random rand = new Random(42); // fixed seed for reproducibility

        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int num = 1 + rand.nextInt(10);
                int denom = 1 + rand.nextInt(10);
                weights[i][j] = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(num), BigInteger.valueOf(denom));
            }
            // Bias = 0 for now (can be arbitrary later)
            biases[i] = ArbitraryNumberV2.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
        }
    }

    public List<ArbitraryNumberV2> forward(List<ArbitraryNumberV2> inputVector) {
        if (inputVector.size() != inputSize) {
            throw new IllegalArgumentException("Input vector size mismatch");
        }

        List<ArbitraryNumberV2> outputs = new ArrayList<>();

        for (int i = 0; i < outputSize; i++) {
            ArbitraryNumberV2 sum = biases[i];

            for (int j = 0; j < inputSize; j++) {
                ArbitraryNumberV2 weighted = ArbitraryNumberV2.multiply(weights[i][j], inputVector.get(j));
                sum = ArbitraryNumberV2.add(sum, weighted);
            }

            outputs.add(sum);
        }

        return outputs;
    }

    public ArbitraryNumberV2[][] getWeights() {
        return weights;
    }

    public ArbitraryNumberV2[] getBiases() {
        return biases;
    }
}
