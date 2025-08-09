package com.github.arbitrary_number;
import java.math.BigInteger;
import java.util.List;

public class SymbolicLayerTest {

    public static void main(String[] args) {
        // Symbolic inputs: [1*(1/100), 1*(1/20), 1*(1000/1)]
        ArbitraryNumberV2 in1 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(100));
        ArbitraryNumberV2 in2 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(20));
        ArbitraryNumberV2 in3 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(1000), BigInteger.ONE);
        List<ArbitraryNumberV2> inputVector = List.of(in1, in2, in3);

        // Create a symbolic dense layer: 3 inputs → 2 outputs
        SymbolicDenseLayer dense = new SymbolicDenseLayer(3, 2);

        // Forward propagation
        List<ArbitraryNumberV2> outputs = dense.forward(inputVector);

        // Display output values (numeric and symbolic)
        System.out.println("Symbolic Dense Layer Outputs:");
        for (int i = 0; i < outputs.size(); i++) {
            ArbitraryNumberV2 output = outputs.get(i);
            System.out.println("Output " + i + " (Symbolic): " + output);
            System.out.println("Output " + i + " (Evaluated, precision 40): " + output.evaluate(40));
            System.out.println("Output " + i + " (JSON AST):");
            System.out.println(output.toJson().toString(2));
        }
    }
}
