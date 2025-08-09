package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SoftmaxWithArbitraryPrecisionTest {

    @Test
    public void testSoftmaxExtremeFeatureValues() {
        // Simulate three logits: 1/1000, 1, and 1000
        ArbitraryNumberV2 x1 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(1000)); // 0.001
        ArbitraryNumberV2 x2 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);          // 1.0
        ArbitraryNumberV2 x3 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(1000), BigInteger.ONE); // 1000.0

        List<ArbitraryNumberV2> logits = List.of(x1, x2, x3);
        List<ArbitraryNumberV2> expVals = new ArrayList<>();
        ArbitraryNumberV2 e = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(271828), BigInteger.valueOf(100000)); // e ≈ 2.71828

        // exp(x) ≈ e^x (symbolic)
        for (ArbitraryNumberV2 x : logits) {
            expVals.add(ArbitraryNumberV2.power(e, x));  // e^xi
        }

        // Sum of e^x values
        ArbitraryNumberV2 sum = expVals.get(0);
        for (int i = 1; i < expVals.size(); i++) {
            sum = ArbitraryNumberV2.add(sum, expVals.get(i));
        }

        // Compute softmax probabilities
        System.out.println("Softmax Probabilities:");
        for (int i = 0; i < logits.size(); i++) {
            ArbitraryNumberV2 softmaxProb = ArbitraryNumberV2.divide(expVals.get(i), sum);
            BigDecimal value = softmaxProb.evaluate(50);
            System.out.printf("  Input x%d = %s  -->  Softmax = %s\n", i + 1, logits.get(i), value.toPlainString());
        }

        // Contrast with naive floating-point
        double dx1 = 0.001;
        double dx2 = 1.0;
        double dx3 = 1000.0;

        double edx1 = Math.exp(dx1);
        double edx2 = Math.exp(dx2);
        double edx3 = Math.exp(dx3); // This will likely be infinity

        double sumExp = edx1 + edx2 + edx3;

        System.out.println("\nFloating Point Softmax (double):");
        System.out.printf("  x1 = %.3f --> %.5f\n", dx1, edx1 / sumExp);
        System.out.printf("  x2 = %.3f --> %.5f\n", dx2, edx2 / sumExp);
        System.out.printf("  x3 = %.3f --> %.5f\n", dx3, edx3 / sumExp);
    }
}
