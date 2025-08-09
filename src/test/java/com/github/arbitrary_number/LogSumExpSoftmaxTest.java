package com.github.arbitrary_number;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LogSumExpSoftmaxTest {

    @Test
    public void testLogSumExpSoftmaxPrecision() {
        // Extreme and imbalanced inputs
        ArbitraryNumberV2 x1 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(200));    // 0.005
        ArbitraryNumberV2 x2 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(50));     // 0.02
        ArbitraryNumberV2 x3 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(500), BigInteger.ONE);    // 500.0

        List<ArbitraryNumberV2> inputs = List.of(x1, x2, x3);

        // Find max(x)
        ArbitraryNumberV2 max = x1;
        for (ArbitraryNumberV2 x : inputs) {
            if (x.evaluate(10).compareTo(max.evaluate(10)) > 0) {
                max = x;
            }
        }

        ArbitraryNumberV2 e = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(271828), BigInteger.valueOf(100000)); // Approx e

        List<ArbitraryNumberV2> expShifted = new ArrayList<>();
        for (ArbitraryNumberV2 x : inputs) {
            ArbitraryNumberV2 shifted = ArbitraryNumberV2.subtract(x, max);         // x - max(x)
            expShifted.add(ArbitraryNumberV2.power(e, shifted));                    // exp(x - max)
        }

        // Sum of exponentials
        ArbitraryNumberV2 sum = expShifted.get(0);
        for (int i = 1; i < expShifted.size(); i++) {
            sum = ArbitraryNumberV2.add(sum, expShifted.get(i));
        }

        System.out.println("Softmax Probabilities (with LogSumExp Trick):");
        for (int i = 0; i < inputs.size(); i++) {
            ArbitraryNumberV2 prob = ArbitraryNumberV2.divide(expShifted.get(i), sum);
            BigDecimal value = prob.evaluate(50);
            System.out.printf("  Input x%d = %s  -->  Softmax = %s\n", i + 1, inputs.get(i), value.toPlainString());
        }

        // Floating point softmax with log-sum-exp
        double[] floats = {200.0, 50.0, 500.0};
        double maxFloat = Math.max(floats[0], Math.max(floats[1], floats[2]));
        double sumExpFloat = 0.0;
        for (double v : floats) {
            sumExpFloat += Math.exp(v - maxFloat);
        }
        System.out.println("\nFloat Softmax with LogSumExp:");
        for (int i = 0; i < floats.length; i++) {
            double s = Math.exp(floats[i] - maxFloat) / sumExpFloat;
            System.out.printf("  x%d = %.3f  --> Softmax = %.8f\n", i + 1, floats[i], s);
        }
    }
}
