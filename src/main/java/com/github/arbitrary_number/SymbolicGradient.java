package com.github.arbitrary_number;
import java.math.BigInteger;

public class SymbolicGradient {

    public static SymbolicExpression computeGradient(SymbolicExpression expr, String var) {
        switch (expr.op) {
            case TERM:
                // Constant term derivative = 0
                return SymbolicExpression.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);

            case VARIABLE:
                // d/dx x = 1, d/dx y = 0
                if (var.equals(expr.variableName)) {
                    return SymbolicExpression.term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                } else {
                    return SymbolicExpression.term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                }

            case ADD:
                // (f + g)' = f' + g'
                return SymbolicExpression.add(
                        computeGradient(expr.children.get(0), var),
                        computeGradient(expr.children.get(1), var)
                );

            case SUBTRACT:
                // (f - g)' = f' - g'
                return SymbolicExpression.subtract(
                        computeGradient(expr.children.get(0), var),
                        computeGradient(expr.children.get(1), var)
                );

            case MULTIPLY:
                // (f * g)' = f'*g + f*g'
                SymbolicExpression f = expr.children.get(0);
                SymbolicExpression g = expr.children.get(1);
                return SymbolicExpression.add(
                        SymbolicExpression.multiply(computeGradient(f, var), g),
                        SymbolicExpression.multiply(f, computeGradient(g, var))
                );

            case DIVIDE:
                // (f / g)' = (f'*g - f*g') / (g^2)
                f = expr.children.get(0);
                g = expr.children.get(1);
                SymbolicExpression numerator = SymbolicExpression.subtract(
                        SymbolicExpression.multiply(computeGradient(f, var), g),
                        SymbolicExpression.multiply(f, computeGradient(g, var))
                );
                SymbolicExpression denominator = SymbolicExpression.power(g, SymbolicExpression.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE));
                return SymbolicExpression.divide(numerator, denominator);

            case POWER:
                // d/dx f^g = f^g * [ g'*ln(f) + g * f'/f ]
                SymbolicExpression base = expr.children.get(0);
                SymbolicExpression exp = expr.children.get(1);
                SymbolicExpression fPrime = computeGradient(base, var);
                SymbolicExpression gPrime = computeGradient(exp, var);

                // ln(f)
                SymbolicExpression lnF = SymbolicExpression.log(base);

                // g'*ln(f)
                SymbolicExpression term1 = SymbolicExpression.multiply(gPrime, lnF);

                // f'/f
                SymbolicExpression term2 = SymbolicExpression.divide(fPrime, base);

                // g * (f'/f)
                SymbolicExpression term3 = SymbolicExpression.multiply(exp, term2);

                // sum inside brackets
                SymbolicExpression bracket = SymbolicExpression.add(term1, term3);

                // full derivative
                return SymbolicExpression.multiply(expr, bracket);

            case LOG:
                // d/dx log(f) = f'/f
                SymbolicExpression arg = expr.children.get(0);
                SymbolicExpression argPrime = computeGradient(arg, var);
                return SymbolicExpression.divide(argPrime, arg);

            default:
                throw new UnsupportedOperationException("Gradient not implemented for op: " + expr.op);
        }
    }
}
