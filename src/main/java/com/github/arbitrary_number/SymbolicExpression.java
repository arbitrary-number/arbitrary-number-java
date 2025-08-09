package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // Method to compute symbolic derivative with respect to a given variable
    public SymbolicExpression differentiate(String variableName) {
        switch (op) {
            case TERM:
                // Constants have zero derivative
                return term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            case VARIABLE:
                // d(x)/dx = 1
                return variableName.equals(this.variableName)
                        ? term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
                        : term(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
            case ADD:
                return add(children.get(0).differentiate(variableName),
                           children.get(1).differentiate(variableName));
            case SUBTRACT:
                return subtract(children.get(0).differentiate(variableName),
                                children.get(1).differentiate(variableName));
            case MULTIPLY:
                return add(multiply(children.get(0).differentiate(variableName), children.get(1)),
                           multiply(children.get(0), children.get(1).differentiate(variableName)));
            case DIVIDE:
                return divide(
                        subtract(multiply(children.get(0).differentiate(variableName), children.get(1)),
                                 multiply(children.get(0), children.get(1).differentiate(variableName))),
                        multiply(children.get(1), children.get(1)));
            case POWER:
                SymbolicExpression base = children.get(0);
                SymbolicExpression exp = children.get(1);
                SymbolicExpression baseDerivative = base.differentiate(variableName);
                return multiply(multiply(exp, power(base, subtract(exp, term(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)))), baseDerivative);
            case LOG:
                return divide(children.get(0).differentiate(variableName), children.get(0));
            default:
                throw new UnsupportedOperationException("Unknown operation: " + op);
        }
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

    // Evaluate the symbolic expression for given variable values
    public double evaluate(Map<String, Double> variableValues) {
        switch (op) {
            case TERM:
                return coefficient.doubleValue() * (numerator.doubleValue() / denominator.doubleValue());
            case VARIABLE:
                if (variableValues.containsKey(variableName)) {
                    return variableValues.get(variableName);
                } else {
                    throw new IllegalArgumentException("Variable " + variableName + " not found in evaluation map.");
                }
            case ADD:
                return children.get(0).evaluate(variableValues) + children.get(1).evaluate(variableValues);
            case SUBTRACT:
                return children.get(0).evaluate(variableValues) - children.get(1).evaluate(variableValues);
            case MULTIPLY:
                return children.get(0).evaluate(variableValues) * children.get(1).evaluate(variableValues);
            case DIVIDE:
                return children.get(0).evaluate(variableValues) / children.get(1).evaluate(variableValues);
            case POWER:
                return Math.pow(children.get(0).evaluate(variableValues), children.get(1).evaluate(variableValues));
            case LOG:
                return Math.log(children.get(0).evaluate(variableValues));
            default:
                throw new UnsupportedOperationException("Unknown operation: " + op);
        }
    }

    // Evaluate the derivative for given variable values
    public double evaluateDerivative(Map<String, Double> variableValues, String variableName) {
        SymbolicExpression derivative = this.differentiate(variableName);
        return derivative.evaluate(variableValues);
    }

    // Simplify the expression to remove trivial operations
    public SymbolicExpression simplify() {
        switch (op) {
            case TERM:
            case VARIABLE:
                return this;
            case ADD: {
                SymbolicExpression left = children.get(0).simplify();
                SymbolicExpression right = children.get(1).simplify();
                if (isZero(left)) return right;
                if (isZero(right)) return left;
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

    // Helper methods for simplification
    private boolean isZero(SymbolicExpression n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ZERO);
    }

    private boolean isOne(SymbolicExpression n) {
        return n.op == Operation.TERM && n.coefficient.equals(BigInteger.ONE) &&
               n.numerator.equals(BigInteger.ONE) && n.denominator.equals(BigInteger.ONE);
    }

    // Differentiation (same as before)...

    // Symbolic gradient (same as before)...

    // Convert expression to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("operation", op.name());
        json.put("coefficient", coefficient.toString());
        json.put("numerator", numerator.toString());
        json.put("denominator", denominator.toString());
        if (variableName != null) json.put("variable", variableName);
        if (!children.isEmpty()) {
            JSONArray childrenArray = new JSONArray();
            for (SymbolicExpression child : children) {
                childrenArray.put(child.toJson());
            }
            json.put("children", childrenArray);
        }
        return json;
    }

    @Override
    public String toString() {
        switch (op) {
            case TERM:
                return coefficient + " * (" + numerator + "/" + denominator + ")";
            case VARIABLE:
                return variableName;
            case ADD:
                return "(" + children.get(0) + " + " + children.get(1) + ")";
            case SUBTRACT:
                return "(" + children.get(0) + " - " + children.get(1) + ")";
            case MULTIPLY:
                return "(" + children.get(0) + " * " + children.get(1) + ")";
            case DIVIDE:
                return "(" + children.get(0) + " / " + children.get(1) + ")";
            case POWER:
                return "(" + children.get(0) + " ^ " + children.get(1) + ")";
            case LOG:
                return "log(" + children.get(0) + ")";
            default:
                return "Unknown operation";
        }
    }
}