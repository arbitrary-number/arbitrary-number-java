package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SymbolicExpression {
    enum Operation {
        TERM, ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, LOG, VARIABLE
    }

    Operation op;
    List<SymbolicExpression> children = new ArrayList<>();
    BigInteger coefficient = BigInteger.ONE;
    BigInteger numerator = BigInteger.ONE;
    BigInteger denominator = BigInteger.ONE;
    String variableName;

    // Factory methods
    public static SymbolicExpression term(BigInteger coef, BigInteger num, BigInteger denom) {
        SymbolicExpression n = new SymbolicExpression();
        n.op = Operation.TERM;
        n.coefficient = coef;
        n.numerator = num;
        n.denominator = denom;
        return n;
    }

    public static SymbolicExpression variable(String name) {
        SymbolicExpression n = new SymbolicExpression();
        n.op = Operation.VARIABLE;
        n.variableName = name;
        return n;
    }

    public static SymbolicExpression add(SymbolicExpression a, SymbolicExpression b) {
        return node(Operation.ADD, a, b);
    }

    public static SymbolicExpression subtract(SymbolicExpression a, SymbolicExpression b) {
        return node(Operation.SUBTRACT, a, b);
    }

    public static SymbolicExpression multiply(SymbolicExpression a, SymbolicExpression b) {
        return node(Operation.MULTIPLY, a, b);
    }

    public static SymbolicExpression divide(SymbolicExpression a, SymbolicExpression b) {
        return node(Operation.DIVIDE, a, b);
    }

    public static SymbolicExpression power(SymbolicExpression base, SymbolicExpression exp) {
        return node(Operation.POWER, base, exp);
    }

    public static SymbolicExpression log(SymbolicExpression arg) {
        SymbolicExpression n = new SymbolicExpression();
        n.op = Operation.LOG;
        n.children.add(arg);
        return n;
    }

    public static SymbolicExpression node(Operation op, SymbolicExpression... nodes) {
        SymbolicExpression n = new SymbolicExpression();
        n.op = op;
        for (SymbolicExpression c : nodes) {
            n.children.add(c);
        }
        return n;
    }

    public static SymbolicExpression negate(SymbolicExpression number) {
        // Create a TERM node representing -1
        SymbolicExpression negativeOne = term(BigInteger.valueOf(-1), BigInteger.ONE, BigInteger.ONE);
        // Multiply -1 by the input number
        return multiply(negativeOne, number);
    }

    // Log with base: log_base(argument)
    public static SymbolicExpression log(SymbolicExpression base, SymbolicExpression argument) {
        // Represent as: log(argument) / log(base)
        SymbolicExpression logArg = log(argument);
        SymbolicExpression logBase = log(base);
        return divide(logArg, logBase);
    }

    // Overload for convenience: log(baseNumerator/baseDenominator, argNumerator/argDenominator)
    public static SymbolicExpression log(BigInteger baseNumerator, BigInteger baseDenominator,
                                        BigInteger argNumerator, BigInteger argDenominator) {
        SymbolicExpression base = term(BigInteger.ONE, baseNumerator, baseDenominator);
        SymbolicExpression arg = term(BigInteger.ONE, argNumerator, argDenominator);
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
            case VARIABLE -> throw new UnsupportedOperationException("Cannot evaluate symbolic variable directly.");
            case ADD -> {
                BigDecimal sum = BigDecimal.ZERO;
                for (SymbolicExpression c : children) sum = sum.add(c.evaluate(precision), mc);
                return sum;
            }
            case SUBTRACT -> {
                return children.get(0).evaluate(precision).subtract(children.get(1).evaluate(precision), mc);
            }
            case MULTIPLY -> {
                BigDecimal prod = BigDecimal.ONE;
                for (SymbolicExpression c : children) prod = prod.multiply(c.evaluate(precision), mc);
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

    public SymbolicExpression simplify() {
        switch (op) {
            case TERM:
            case VARIABLE:
                return this; // already simplest
            case ADD: {
                SymbolicExpression left = children.get(0).simplify();
                SymbolicExpression right = children.get(1).simplify();
                // if left == 0 return right
                if (isZero(left)) return right;
                // if right == 0 return left
                if (isZero(right)) return left;
                // if both terms are TERM, combine coefficients if possible (optional)
                return add(left, right);
            }
            case SUBTRACT: {
                SymbolicExpression left = children.get(0).simplify();
                SymbolicExpression right = children.get(1).simplify();
                if (isZero(right)) return left;
                return subtract(left, right);
            }
            case MULTIPLY: {
                SymbolicExpression left = children.get(0).simplify();
                SymbolicExpression right = children.get(1).simplify();
                if (isZero(left) || isZero(right)) return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                if (isOne(left)) return right;
                if (isOne(right)) return left;
                return multiply(left, right);
            }
            case DIVIDE: {
                SymbolicExpression left = children.get(0).simplify();
                SymbolicExpression right = children.get(1).simplify();
                if (isZero(left)) return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                if (isOne(right)) return left;
                return divide(left, right);
            }
            case POWER: {
                SymbolicExpression base = children.get(0).simplify();
                SymbolicExpression exp = children.get(1).simplify();
                if (isZero(exp)) return term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                if (isOne(exp)) return base;
                return power(base, exp);
            }
            case LOG: {
                SymbolicExpression arg = children.get(0).simplify();
                return log(arg);
            }
            default:
                return this;
        }
    }

    private boolean isZero(SymbolicExpression n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ZERO);
    }

    private boolean isOne(SymbolicExpression n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ONE) &&
               n.numerator.equals(BigInteger.ONE) && n.denominator.equals(BigInteger.ONE);
    }


    public SymbolicExpression differentiate(String variableName) {
        switch (op) {
            case TERM -> {
                // Constants have zero derivative
                return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            }
            case VARIABLE -> {
                // d(x)/dx = 1
                return variableName.equals(this.variableName)
                        ? term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
                        : term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            }
            case ADD -> {
                return add(children.get(0).differentiate(variableName), children.get(1).differentiate(variableName));
            }
            case SUBTRACT -> {
                return subtract(children.get(0).differentiate(variableName), children.get(1).differentiate(variableName));
            }
            case MULTIPLY -> {
                // Product rule: u'v + uv'
                SymbolicExpression u = children.get(0);
                SymbolicExpression v = children.get(1);
                return add(
                        multiply(u.differentiate(variableName), v),
                        multiply(u, v.differentiate(variableName))
                );
            }
            case DIVIDE -> {
                // Quotient rule: (u'v - uv') / v²
                SymbolicExpression u = children.get(0);
                SymbolicExpression v = children.get(1);
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
                SymbolicExpression base = children.get(0);
                SymbolicExpression exp = children.get(1);

                SymbolicExpression one = term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
                SymbolicExpression expMinusOne = subtract(exp, one);
                return multiply(
                        multiply(exp, power(base, expMinusOne)),
                        base.differentiate(variableName)
                );
            }
            case LOG -> {
                SymbolicExpression u = children.get(0);
                return divide(u.differentiate(variableName), u);
            }
            default -> throw new UnsupportedOperationException("Differentiation not implemented for op: " + op);
        }
    }

    public SymbolicExpression symbolicGrad(SymbolicExpression inputVar) {
        switch (op) {
            case TERM -> {
                // Gradient is 1 if this term == inputVar else 0
                return this.equals(inputVar) ? term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
                                            : term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            }
            case VARIABLE -> {
                // Gradient is 1 if variableName matches inputVar's variableName
                if (this.op == Operation.VARIABLE && inputVar.op == Operation.VARIABLE) {
                    return this.variableName.equals(inputVar.variableName) ?
                        term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE) :
                        term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                } else {
                    return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
                }
            }
            case ADD -> {
                return add(children.get(0).symbolicGrad(inputVar), children.get(1).symbolicGrad(inputVar));
            }
            case SUBTRACT -> {
                return subtract(children.get(0).symbolicGrad(inputVar), children.get(1).symbolicGrad(inputVar));
            }
            case MULTIPLY -> {
                SymbolicExpression left = children.get(0);
                SymbolicExpression right = children.get(1);
                SymbolicExpression leftGrad = left.symbolicGrad(inputVar);
                SymbolicExpression rightGrad = right.symbolicGrad(inputVar);
                return add(multiply(leftGrad, right), multiply(left, rightGrad));
            }
            case DIVIDE -> {
                SymbolicExpression left = children.get(0);
                SymbolicExpression right = children.get(1);
                SymbolicExpression leftGrad = left.symbolicGrad(inputVar);
                SymbolicExpression rightGrad = right.symbolicGrad(inputVar);
                SymbolicExpression numerator = subtract(multiply(leftGrad, right), multiply(left, rightGrad));
                SymbolicExpression denominator = power(right, term(BigInteger.TWO, BigInteger.ONE, BigInteger.ONE));
                return divide(numerator, denominator);
            }
            case POWER -> {
                SymbolicExpression base = children.get(0);
                SymbolicExpression exp = children.get(1);
                SymbolicExpression baseGrad = base.symbolicGrad(inputVar);
                SymbolicExpression expGrad = exp.symbolicGrad(inputVar);

                // f^g * (g' * ln(f) + g * f'/f)
                SymbolicExpression lnBase = log(base);
                SymbolicExpression term1 = multiply(expGrad, lnBase);
                SymbolicExpression term2 = multiply(exp, divide(baseGrad, base));
                SymbolicExpression inside = add(term1, term2);

                return multiply(this, inside); // this = f^g
            }
            case LOG -> {
                SymbolicExpression arg = children.get(0);
                SymbolicExpression argGrad = arg.symbolicGrad(inputVar);
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

        SymbolicExpression other = (SymbolicExpression) obj;

        if (this.op != other.op) return false;

        switch (this.op) {
            case TERM:
                return this.coefficient.equals(other.coefficient) &&
                       this.numerator.equals(other.numerator) &&
                       this.denominator.equals(other.denominator);
            case VARIABLE:
                return this.variableName != null && this.variableName.equals(other.variableName);
            default:
                if (this.children.size() != other.children.size()) return false;
                for (int i = 0; i < this.children.size(); i++) {
                    if (!this.children.get(i).equals(other.children.get(i))) return false;
                }
                return true;
        }
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
            case VARIABLE -> {
                json.put("name", variableName);
            }
            default -> {
                JSONArray args = new JSONArray();
                for (SymbolicExpression c : children) {
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
            case VARIABLE -> variableName;
            case ADD -> "(" + children.get(0) + " + " + children.get(1) + ")";
            case SUBTRACT -> "(" + children.get(0) + " - " + children.get(1) + ")";
            case MULTIPLY -> "(" + children.get(0) + " * " + children.get(1) + ")";
            case DIVIDE -> "(" + children.get(0) + " / " + children.get(1) + ")";
            case POWER -> "(" + children.get(0) + " ^ " + children.get(1) + ")";
            case LOG -> "log(" + children.get(0) + ")";
        };
    }
}
