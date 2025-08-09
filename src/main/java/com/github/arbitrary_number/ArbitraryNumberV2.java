package com.github.arbitrary_number;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.arbitrary_number.ArbitraryNumberV2Alpha.Op;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class ArbitraryNumberV2 {
    enum Operation {
        TERM, ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, LOG, VARIABLE
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

    public static ArbitraryNumberV2 variable(String name) {
        ArbitraryNumberV2 n = new ArbitraryNumberV2();
        n.op = Operation.VARIABLE;
        n.variableName = name;
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



    private static ArbitraryNumberV2 node(Operation op, ArbitraryNumberV2... nodes) {
        ArbitraryNumberV2 n = new ArbitraryNumberV2();
        n.op = op;
        for (ArbitraryNumberV2 c : nodes) {
            n.children.add(c);
        }
        return n;
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
