package com.github.arbitrary_number;
import java.math.BigInteger;

public class SymbolicGradient {

    public static ArbitraryNumberV2 computeGradient(ArbitraryNumberV2 expr, String var) {
        switch (expr.op) {
            case TERM:
                // Constant term derivative = 0
                return ArbitraryNumberV2.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);

            case VARIABLE:
                // d/dx x = 1, d/dx y = 0
                if (var.equals(expr.variableName)) {
                    return ArbitraryNumberV2.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                } else {
                    return ArbitraryNumberV2.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                }

            case ADD:
                // (f + g)' = f' + g'
                return ArbitraryNumberV2.add(
                        computeGradient(expr.children.get(0), var),
                        computeGradient(expr.children.get(1), var)
                );

            case SUBTRACT:
                // (f - g)' = f' - g'
                return ArbitraryNumberV2.subtract(
                        computeGradient(expr.children.get(0), var),
                        computeGradient(expr.children.get(1), var)
                );

            case MULTIPLY:
                // (f * g)' = f'*g + f*g'
                ArbitraryNumberV2 f = expr.children.get(0);
                ArbitraryNumberV2 g = expr.children.get(1);
                return ArbitraryNumberV2.add(
                        ArbitraryNumberV2.multiply(computeGradient(f, var), g),
                        ArbitraryNumberV2.multiply(f, computeGradient(g, var))
                );

            case DIVIDE:
                // (f / g)' = (f'*g - f*g') / (g^2)
                f = expr.children.get(0);
                g = expr.children.get(1);
                ArbitraryNumberV2 numerator = ArbitraryNumberV2.subtract(
                        ArbitraryNumberV2.multiply(computeGradient(f, var), g),
                        ArbitraryNumberV2.multiply(f, computeGradient(g, var))
                );
                ArbitraryNumberV2 denominator = ArbitraryNumberV2.power(g, ArbitraryNumberV2.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE));
                return ArbitraryNumberV2.divide(numerator, denominator);

            case POWER:
                // d/dx f^g = f^g * [ g'*ln(f) + g * f'/f ]
                ArbitraryNumberV2 base = expr.children.get(0);
                ArbitraryNumberV2 exp = expr.children.get(1);
                ArbitraryNumberV2 fPrime = computeGradient(base, var);
                ArbitraryNumberV2 gPrime = computeGradient(exp, var);

                // ln(f)
                ArbitraryNumberV2 lnF = ArbitraryNumberV2.log(base);

                // g'*ln(f)
                ArbitraryNumberV2 term1 = ArbitraryNumberV2.multiply(gPrime, lnF);

                // f'/f
                ArbitraryNumberV2 term2 = ArbitraryNumberV2.divide(fPrime, base);

                // g * (f'/f)
                ArbitraryNumberV2 term3 = ArbitraryNumberV2.multiply(exp, term2);

                // sum inside brackets
                ArbitraryNumberV2 bracket = ArbitraryNumberV2.add(term1, term3);

                // full derivative
                return ArbitraryNumberV2.multiply(expr, bracket);

            case LOG:
                // d/dx log(f) = f'/f
                ArbitraryNumberV2 arg = expr.children.get(0);
                ArbitraryNumberV2 argPrime = computeGradient(arg, var);
                return ArbitraryNumberV2.divide(argPrime, arg);

            default:
                throw new UnsupportedOperationException("Gradient not implemented for op: " + expr.op);
        }
    }
}
