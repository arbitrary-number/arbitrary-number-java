package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.github.arbitrary_number.ArbitraryNumberV2;

public class SymbolicSoftmaxExplainableTest {

    @Test
    public void testExplainableSoftmaxWithAST() {
        // Define extreme input values as symbolic terms
        ArbitraryNumberV2 x1 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(100));
        ArbitraryNumberV2 x2 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(20));
        ArbitraryNumberV2 x3 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(1000), BigInteger.ONE);

        List<ArbitraryNumberV2> inputs = List.of(x1, x2, x3);

        // Find max(x)
        ArbitraryNumberV2 max = x1;
        for (ArbitraryNumberV2 x : inputs) {
            if (x.evaluate(10).compareTo(max.evaluate(10)) > 0) {
                max = x;
            }
        }

        // Define symbolic constant for 'e'
        ArbitraryNumberV2 e = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(271828), BigInteger.valueOf(100000));

        // Compute exp(xi - max) for each input
        List<ArbitraryNumberV2> expShifted = new ArrayList<>();
        for (ArbitraryNumberV2 x : inputs) {
            ArbitraryNumberV2 shifted = ArbitraryNumberV2.subtract(x, max);
            ArbitraryNumberV2 exp = ArbitraryNumberV2.power(e, shifted);
            expShifted.add(exp);
        }

        // Compute sum of exponentials
        ArbitraryNumberV2 sumExp = expShifted.get(0);
        for (int i = 1; i < expShifted.size(); i++) {
            sumExp = ArbitraryNumberV2.add(sumExp, expShifted.get(i));
        }

        // Compute softmax and export each AST
        System.out.println("Symbolic Softmax with Explainable AST (JSON):");
        for (int i = 0; i < inputs.size(); i++) {
            ArbitraryNumberV2 numerator = expShifted.get(i);
            ArbitraryNumberV2 softmax = ArbitraryNumberV2.divide(numerator, sumExp);

            // Evaluate to high precision
            BigDecimal value = softmax.evaluate(50);

            // Print result
            System.out.printf("  Input x%d = %s  --> Softmax = %s\n", i + 1, inputs.get(i), value.toPlainString());

            // Export AST as JSON
            JSONObject astJson = softmax.toJson();
            System.out.println("    AST JSON:");
            System.out.println(astJson.toString(2));
        }
    }
}
