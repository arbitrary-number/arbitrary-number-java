package com.github.arbitrary_number;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ArbitraryNumberV2MLTest {

    @Test
    public void testWeightedFeatureCombination() {
        // Example features as fractions (simulating fractional weights)
        ArbitraryNumberV2 f1 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(3));   // 1/3
        ArbitraryNumberV2 f2 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(7));   // 1/7
        ArbitraryNumberV2 f3 = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(11));  // 1/11

        // Weighted sum with powers and logs:
        // result = (f1^2 + log_2(f2)) * (1 - f3)
        ArbitraryNumberV2 two = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.ONE);  // 2
        ArbitraryNumberV2 one = ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);          // 1

        ArbitraryNumberV2 f1Squared = ArbitraryNumberV2.power(f1, ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.ONE)); // f1^2
        //ArbitraryNumberV2 logTerm = ArbitraryNumberV2.log(two, f2);  // log_2(f2)
        //ArbitraryNumberV2 sum = ArbitraryNumberV2.add(f1Squared, logTerm); // f1^2 + log_2(f2)

        ArbitraryNumberV2 oneMinusF3 = ArbitraryNumberV2.subtract(one, f3); // 1 - f3

        //ArbitraryNumberV2 result = ArbitraryNumberV2.multiply(sum, oneMinusF3);

        // Evaluate with high precision
        //BigDecimal decimalResult = result.evaluate(50);

        // Print symbolic expression and evaluated value
//        System.out.println("Symbolic Expression: " + result);
//        System.out.println("Evaluated Result (50 decimal places): " + decimalResult);
//
//        // --- Compare with naive double precision float eval (expected loss of precision) ---
//        double f1dbl = 1.0 / 3.0;
//        double f2dbl = 1.0 / 7.0;
//        double f3dbl = 1.0 / 11.0;
//
//        double naiveResult = (Math.pow(f1dbl, 2) + (Math.log(f2dbl) / Math.log(2.0))) * (1.0 - f3dbl);
//
//        System.out.println("Naive double float eval: " + naiveResult);
//
//        // Assert close but not exact — show arbitrary precision is better by comparing decimal digits
//        assertTrue(decimalResult.subtract(BigDecimal.valueOf(naiveResult)).abs().compareTo(new BigDecimal("0.0001")) > 0,
//                "ArbitraryNumberV2 precision exceeds floating point precision by large margin");
    }
}
