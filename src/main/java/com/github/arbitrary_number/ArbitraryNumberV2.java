package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ArbitraryNumberV2 {
    enum Operation {
        TERM, ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, LOG
    }

    Operation op;
    List<ArbitraryNumberV2> children = new ArrayList<>();
    BigInteger coefficient = BigInteger.ONE;
    BigInteger numerator = BigInteger.ONE;
    BigInteger denominator = BigInteger.ONE;
    String variableName;

    // Factory methods
    public static ArbitraryNumberV2 term(BigInteger coef, BigInteger num, BigInteger denom) {
        ArbitraryNumberV2 n = new ArbitraryNumberV2();
        n.op = Operation.TERM;
        n.coefficient = coef;
        n.numerator = num;
        n.denominator = denom;
        return n;
    }

    public static ArbitraryNumberV2 add(ArbitraryNumberV2 a, ArbitraryNumberV2 b) {
        return node(Operation.ADD, a, b);
    }

    public static ArbitraryNumberV2 subtract(ArbitraryNumberV2 a, ArbitraryNumberV2 b) {
        return node(Operation.SUBTRACT, a, b);
    }

    public static ArbitraryNumberV2 multiply(ArbitraryNumberV2 a, ArbitraryNumberV2 b) {
        return node(Operation.MULTIPLY, a, b);
    }

    public static ArbitraryNumberV2 divide(ArbitraryNumberV2 a, ArbitraryNumberV2 b) {
        return node(Operation.DIVIDE, a, b);
    }

    public static ArbitraryNumberV2 power(ArbitraryNumberV2 base, ArbitraryNumberV2 exp) {
        return node(Operation.POWER, base, exp);
    }

    public static ArbitraryNumberV2 log(ArbitraryNumberV2 arg) {
        ArbitraryNumberV2 n = new ArbitraryNumberV2();
        n.op = Operation.LOG;
        n.children.add(arg);
        return n;
    }

    public static ArbitraryNumberV2 node(Operation op, ArbitraryNumberV2... nodes) {
        ArbitraryNumberV2 n = new ArbitraryNumberV2();
        n.op = op;
        for (ArbitraryNumberV2 c : nodes) {
            n.children.add(c);
        }
        return n;
    }

    public static ArbitraryNumberV2 negate(ArbitraryNumberV2 number) {
        // Create a TERM node representing -1
        ArbitraryNumberV2 negativeOne = term(BigInteger.valueOf(-1), BigInteger.ONE, BigInteger.ONE);
        // Multiply -1 by the input number
        return multiply(negativeOne, number);
    }

    // Log with base: log_base(argument)
    public static ArbitraryNumberV2 log(ArbitraryNumberV2 base, ArbitraryNumberV2 argument) {
        // Represent as: log(argument) / log(base)
        ArbitraryNumberV2 logArg = log(argument);
        ArbitraryNumberV2 logBase = log(base);
        return divide(logArg, logBase);
    }

    // Overload for convenience: log(baseNumerator/baseDenominator, argNumerator/argDenominator)
    public static ArbitraryNumberV2 log(BigInteger baseNumerator, BigInteger baseDenominator,
                                        BigInteger argNumerator, BigInteger argDenominator) {
        ArbitraryNumberV2 base = term(BigInteger.ONE, baseNumerator, baseDenominator);
        ArbitraryNumberV2 arg = term(BigInteger.ONE, argNumerator, argDenominator);
        return log(base, arg);
    }

    // Evaluate to a BigDecimal (approximate)
    public BigDecimal evaluate(int precision) {
        MathContext mc = new MathContext(precision);
        switch (op) {
            case TERM -> {
                BigDecimal val = new BigDecimal(coefficient).multiply(new BigDecimal(numerator)).divide(new BigDecimal(denominator), mc);
                return val;
            }
            case ADD -> {
                BigDecimal sum = BigDecimal.ZERO;
                for (ArbitraryNumberV2 c : children) sum = sum.add(c.evaluate(precision), mc);
                return sum;
            }
            case SUBTRACT -> {
                return children.get(0).evaluate(precision).subtract(children.get(1).evaluate(precision), mc);
            }
            case MULTIPLY -> {
                BigDecimal prod = BigDecimal.ONE;
                for (ArbitraryNumberV2 c : children) prod = prod.multiply(c.evaluate(precision), mc);
                return prod;
            }
            case DIVIDE -> {
                return children.get(0).evaluate(precision).divide(children.get(1).evaluate(precision), mc);
            }
            case POWER -> {
                BigDecimal base = children.get(0).evaluate(precision);
                BigDecimal exp = children.get(1).evaluate(precision);
                return new BigDecimal(Math.pow(base.doubleValue(), exp.doubleValue()), mc);
            }
            case LOG -> {
                BigDecimal arg = children.get(0).evaluate(precision);
                return new BigDecimal(Math.log(arg.doubleValue()), mc);
            }
            default -> throw new UnsupportedOperationException("Unknown op: " + op);
        }
    }

    public ArbitraryNumberV2 simplify() {
        switch (op) {
            case TERM:
            case ADD: {
                ArbitraryNumberV2 left = children.get(0).simplify();
                ArbitraryNumberV2 right = children.get(1).simplify();
                // if left == 0 return right
                if (isZero(left)) return right;
                // if right == 0 return left
                if (isZero(right)) return left;
                // if both terms are TERM, combine coefficients if possible (optional)
                return add(left, right);
            }
            case SUBTRACT: {
                ArbitraryNumberV2 left = children.get(0).simplify();
                ArbitraryNumberV2 right = children.get(1).simplify();
                if (isZero(right)) return left;
                return subtract(left, right);
            }
            case MULTIPLY: {
                ArbitraryNumberV2 left = children.get(0).simplify();
                ArbitraryNumberV2 right = children.get(1).simplify();
                if (isZero(left) || isZero(right)) return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                if (isOne(left)) return right;
                if (isOne(right)) return left;
                return multiply(left, right);
            }
            case DIVIDE: {
                ArbitraryNumberV2 left = children.get(0).simplify();
                ArbitraryNumberV2 right = children.get(1).simplify();
                if (isZero(left)) return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                if (isOne(right)) return left;
                return divide(left, right);
            }
            case POWER: {
                ArbitraryNumberV2 base = children.get(0).simplify();
                ArbitraryNumberV2 exp = children.get(1).simplify();
                if (isZero(exp)) return term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                if (isOne(exp)) return base;
                return power(base, exp);
            }
            case LOG: {
                ArbitraryNumberV2 arg = children.get(0).simplify();
                return log(arg);
            }
            default:
                return this;
        }
    }

    private boolean isZero(ArbitraryNumberV2 n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ZERO);
    }

    private boolean isOne(ArbitraryNumberV2 n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ONE) &&
               n.numerator.equals(BigInteger.ONE) && n.denominator.equals(BigInteger.ONE);
    }


    public ArbitraryNumberV2 differentiate(String variableName) {
        switch (op) {
            case TERM -> {
                // Constants have zero derivative
                return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            }
            case ADD -> {
                return add(children.get(0).differentiate(variableName), children.get(1).differentiate(variableName));
            }
            case SUBTRACT -> {
                return subtract(children.get(0).differentiate(variableName), children.get(1).differentiate(variableName));
            }
            case MULTIPLY -> {
                // Product rule: u'v + uv'
                ArbitraryNumberV2 u = children.get(0);
                ArbitraryNumberV2 v = children.get(1);
                return add(
                        multiply(u.differentiate(variableName), v),
                        multiply(u, v.differentiate(variableName))
                );
            }
            case DIVIDE -> {
                // Quotient rule: (u'v - uv') / v²
                ArbitraryNumberV2 u = children.get(0);
                ArbitraryNumberV2 v = children.get(1);
                return divide(
                        subtract(
                                multiply(u.differentiate(variableName), v),
                                multiply(u, v.differentiate(variableName))
                        ),
                        power(v, term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE))
                );
            }
            case POWER -> {
                // Assume exponent is constant for now
                ArbitraryNumberV2 base = children.get(0);
                ArbitraryNumberV2 exp = children.get(1);

                ArbitraryNumberV2 one = term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                ArbitraryNumberV2 expMinusOne = subtract(exp, one);
                return multiply(
                        multiply(exp, power(base, expMinusOne)),
                        base.differentiate(variableName)
                );
            }
            case LOG -> {
                ArbitraryNumberV2 u = children.get(0);
                return divide(u.differentiate(variableName), u);
            }
            default -> throw new UnsupportedOperationException("Differentiation not implemented for op: " + op);
        }
    }

    public ArbitraryNumberV2 symbolicGrad(ArbitraryNumberV2 inputVar) {
        switch (op) {
            case TERM -> {
                // Gradient is 1 if this term == inputVar else 0
                return this.equals(inputVar) ? term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
                                            : term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            }

            case ADD -> {
                return add(children.get(0).symbolicGrad(inputVar), children.get(1).symbolicGrad(inputVar));
            }
            case SUBTRACT -> {
                return subtract(children.get(0).symbolicGrad(inputVar), children.get(1).symbolicGrad(inputVar));
            }
            case MULTIPLY -> {
                ArbitraryNumberV2 left = children.get(0);
                ArbitraryNumberV2 right = children.get(1);
                ArbitraryNumberV2 leftGrad = left.symbolicGrad(inputVar);
                ArbitraryNumberV2 rightGrad = right.symbolicGrad(inputVar);
                return add(multiply(leftGrad, right), multiply(left, rightGrad));
            }
            case DIVIDE -> {
                ArbitraryNumberV2 left = children.get(0);
                ArbitraryNumberV2 right = children.get(1);
                ArbitraryNumberV2 leftGrad = left.symbolicGrad(inputVar);
                ArbitraryNumberV2 rightGrad = right.symbolicGrad(inputVar);
                ArbitraryNumberV2 numerator = subtract(multiply(leftGrad, right), multiply(left, rightGrad));
                ArbitraryNumberV2 denominator = power(right, term(BigInteger.TWO, BigInteger.ONE, BigInteger.ONE));
                return divide(numerator, denominator);
            }
            case POWER -> {
                ArbitraryNumberV2 base = children.get(0);
                ArbitraryNumberV2 exp = children.get(1);
                ArbitraryNumberV2 baseGrad = base.symbolicGrad(inputVar);
                ArbitraryNumberV2 expGrad = exp.symbolicGrad(inputVar);

                // f^g * (g' * ln(f) + g * f'/f)
                ArbitraryNumberV2 lnBase = log(base);
                ArbitraryNumberV2 term1 = multiply(expGrad, lnBase);
                ArbitraryNumberV2 term2 = multiply(exp, divide(baseGrad, base));
                ArbitraryNumberV2 inside = add(term1, term2);

                return multiply(this, inside); // this = f^g
            }
            case LOG -> {
                ArbitraryNumberV2 arg = children.get(0);
                ArbitraryNumberV2 argGrad = arg.symbolicGrad(inputVar);
                return divide(argGrad, arg);
            }
            default -> {
                throw new UnsupportedOperationException("Grad not supported for op: " + op);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ArbitraryNumberV2 other = (ArbitraryNumberV2) obj;

        if (this.op != other.op) return false;

        switch (this.op) {
            case TERM:
                return this.coefficient.equals(other.coefficient) &&
                       this.numerator.equals(other.numerator) &&
                       this.denominator.equals(other.denominator);
            default:
                if (this.children.size() != other.children.size()) return false;
                for (int i = 0; i < this.children.size(); i++) {
                    if (!this.children.get(i).equals(other.children.get(i))) return false;
                }
                return true;
        }
    }

    public static ArbitraryNumberV2 exp(ArbitraryNumberV2 exponent) {
        // Approximate e ≈ 2.718281828459045235 with 18 decimal places
        BigInteger eNumerator = new BigInteger("2718281828459045235");
        BigInteger eDenominator = new BigInteger("1000000000000000000");
        ArbitraryNumberV2 eTerm = term(BigInteger.ONE, eNumerator, eDenominator);
        return power(eTerm, exponent);
    }


    // ✅ Recursively convert full AST to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("op", op.toString());

        switch (op) {
            case TERM -> {
                json.put("coefficient", coefficient.toString());
                json.put("numerator", numerator.toString());
                json.put("denominator", denominator.toString());
            }
            default -> {
                JSONArray args = new JSONArray();
                for (ArbitraryNumberV2 c : children) {
                    args.put(c.toJson()); // recursive call
                }
                json.put("args", args);
            }
        }

        return json;
    }

    @Override
    public String toString() {
        return switch (op) {
            case TERM -> coefficient + "*(" + numerator + "/" + denominator + ")";
            case ADD -> "(" + children.get(0) + " + " + children.get(1) + ")";
            case SUBTRACT -> "(" + children.get(0) + " - " + children.get(1) + ")";
            case MULTIPLY -> "(" + children.get(0) + " * " + children.get(1) + ")";
            case DIVIDE -> "(" + children.get(0) + " / " + children.get(1) + ")";
            case POWER -> "(" + children.get(0) + " ^ " + children.get(1) + ")";
            case LOG -> "log(" + children.get(0) + ")";
        };
    }
}
