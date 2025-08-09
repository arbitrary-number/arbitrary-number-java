package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class ArbitraryNumberV2Alpha {

    public abstract BigDecimal evaluate(int precision);

    public abstract String toString();

    // Factory methods:
    public static ArbitraryNumberV2 term(BigInteger c, BigInteger a, BigInteger b) {
        return new TermNode(c, a, b);
    }

    public static ArbitraryNumberV2 add(ArbitraryNumberV2 left, ArbitraryNumberV2 right) {
        return new OperationNode(Op.ADD, left, right);
    }

    public static ArbitraryNumberV2 subtract(ArbitraryNumberV2 left, ArbitraryNumberV2 right) {
        return new OperationNode(Op.SUBTRACT, left, right);
    }

    public static ArbitraryNumberV2 multiply(ArbitraryNumberV2 left, ArbitraryNumberV2 right) {
        return new OperationNode(Op.MULTIPLY, left, right);
    }

    public static ArbitraryNumberV2 divide(ArbitraryNumberV2 left, ArbitraryNumberV2 right) {
        return new OperationNode(Op.DIVIDE, left, right);
    }

    public static ArbitraryNumberV2 negate(ArbitraryNumberV2 value) {
        return new OperationNode(Op.NEGATE, value, null);
    }

    public static ArbitraryNumberV2 power(ArbitraryNumberV2 base, ArbitraryNumberV2 exponent) {
        return new OperationNode(Op.POWER, base, exponent);
    }

    public static ArbitraryNumberV2 log(ArbitraryNumberV2 base, ArbitraryNumberV2 argument) {
        return new OperationNode(Op.LOG, base, argument);
    }

    enum Op {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, NEGATE, POWER, LOG
    }

    private static class TermNode extends ArbitraryNumberV2 {
        BigInteger c, a, b;

        TermNode(BigInteger c, BigInteger a, BigInteger b) {
            if (b.equals(BigInteger.ZERO)) throw new IllegalArgumentException("Denominator cannot be zero.");
            this.c = c;
            this.a = a;
            this.b = b;
        }

        @Override
        public BigDecimal evaluate(int precision) {
            BigDecimal numerator = new BigDecimal(c.multiply(a));
            BigDecimal denominator = new BigDecimal(b);
            return numerator.divide(denominator, precision, RoundingMode.HALF_UP);
        }

        @Override
        public String toString() {
            return c + "*(" + a + "/" + b + ")";
        }
    }

    private static class OperationNode extends ArbitraryNumberV2 {
        Op op;
        ArbitraryNumberV2 left, right;

        OperationNode(Op op, ArbitraryNumberV2 left, ArbitraryNumberV2 right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public BigDecimal evaluate(int precision) {
            MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);

            switch (op) {
                case ADD:
                    return left.evaluate(precision + 5).add(right.evaluate(precision + 5), mc);

                case SUBTRACT:
                    return left.evaluate(precision + 5).subtract(right.evaluate(precision + 5), mc);

                case MULTIPLY:
                    return left.evaluate(precision + 5).multiply(right.evaluate(precision + 5), mc);

                case DIVIDE:
                    BigDecimal r = right.evaluate(precision + 5);
                    if (r.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("Divide by zero");
                    return left.evaluate(precision + 5).divide(r, precision, RoundingMode.HALF_UP);

                case NEGATE:
                    return left.evaluate(precision + 5).negate(mc);

                case POWER:
                    BigDecimal base = left.evaluate(precision + 5);
                    BigDecimal exponent = right.evaluate(precision + 5);
                    // Use BigDecimal.pow only for integer exponents
                    if (exponent.stripTrailingZeros().scale() <= 0) {
                        return base.pow(exponent.intValueExact(), mc);
                    } else {
                        // For fractional exponents, use exp(log(base)*exponent)
                        double val = Math.exp(Math.log(base.doubleValue()) * exponent.doubleValue());
                        return new BigDecimal(val, mc);
                    }

                case LOG:
                    // log base `left` of argument `right`: log(argument) / log(base)
                    BigDecimal arg = right.evaluate(precision + 10);
                    BigDecimal baseLog = left.evaluate(precision + 10);
                    if (arg.compareTo(BigDecimal.ZERO) <= 0 || baseLog.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ArithmeticException("Logarithm of non-positive number");
                    }
                    double logArg = Math.log(arg.doubleValue());
                    double logBase = Math.log(baseLog.doubleValue());
                    double logResult = logArg / logBase;
                    return new BigDecimal(logResult, mc);

                default:
                    throw new IllegalStateException("Unknown operation: " + op);
            }
        }

        @Override
        public String toString() {
            switch (op) {
                case NEGATE:
                    return "-(" + left.toString() + ")";
                case POWER:
                    return "(" + left.toString() + ")^(" + right.toString() + ")";
                case LOG:
                    return "log_(" + left.toString() + ")(" + right.toString() + ")";
                default:
                    return "(" + left.toString() + " " + opToSymbol(op) + " " + right.toString() + ")";
            }
        }

        private String opToSymbol(Op op) {
            switch (op) {
                case ADD: return "+";
                case SUBTRACT: return "-";
                case MULTIPLY: return "*";
                case DIVIDE: return "/";
                default: return "?";
            }
        }
    }


}
