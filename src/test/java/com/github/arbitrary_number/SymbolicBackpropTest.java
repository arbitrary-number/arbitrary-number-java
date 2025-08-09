package com.github.arbitrary_number;

import java.math.BigInteger;

public class SymbolicBackpropTest {
    public static void main(String[] args) {
        ArbitraryNumberV2[] inputs = {
            ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(100)),
            ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(20)),
            ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(1000), BigInteger.ONE)
        };

        ArbitraryNumberV2[][] weights = {
            {
                ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(4)),
                ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(9), BigInteger.valueOf(5)),
                ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(6))
            }
        };

        ArbitraryNumberV2[] biases = {
            ArbitraryNumberV2.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE)
        };

        SymbolicDenseLayerWithGradients layer = new SymbolicDenseLayerWithGradients(weights, biases);
        SymbolicDenseLayerWithGradients.Result result = layer.forwardWithGradients(inputs);

        System.out.println("Symbolic Output: " + result.outputs[0]);
        System.out.println("Evaluated Output (prec=40): " + result.outputs[0].evaluate(40));
        System.out.println("Output Gradient w.r.t Inputs:");
        for (int i = 0; i < result.inputGradients[0].length; i++) {
            System.out.println("∂Output/∂Input[" + i + "] = " + result.inputGradients[0][i]);
        }
    }
}
