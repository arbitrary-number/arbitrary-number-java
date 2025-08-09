package com.github.arbitrary_number;

import java.math.BigDecimal;

public interface SymbolicNode {
    SymbolicExpression forward();  // symbolic expression for output
    SymbolicExpression backward(String variableName); // symbolic derivative wrt variable

    // Evaluate node numerically given input assignments for variables
    default BigDecimal evaluate(java.util.Map<String, BigDecimal> variableAssignments, int precision) {
        SymbolicExpression expr = forward();
        // Replace variables in expr by values from map and evaluate
        return evaluateSymbolicExpression(expr, variableAssignments, precision);
    }

    static BigDecimal evaluateSymbolicExpression(SymbolicExpression expr, java.util.Map<String, BigDecimal> varMap, int precision) {
        switch (expr.op) {
            case TERM -> {
                BigDecimal val = new BigDecimal(expr.coefficient)
                    .multiply(new BigDecimal(expr.numerator))
                    .divide(new BigDecimal(expr.denominator), precision, BigDecimal.ROUND_HALF_UP);
                return val;
            }
            case VARIABLE -> {
                BigDecimal val = varMap.get(expr.variableName);
                if (val == null) throw new IllegalArgumentException("Variable " + expr.variableName + " not provided.");
                return val;
            }
            case ADD -> {
                BigDecimal sum = BigDecimal.ZERO;
                for (SymbolicExpression c : expr.children) {
                    sum = sum.add(evaluateSymbolicExpression(c, varMap, precision));
                }
                return sum;
            }
            case SUBTRACT -> {
                return evaluateSymbolicExpression(expr.children.get(0), varMap, precision)
                    .subtract(evaluateSymbolicExpression(expr.children.get(1), varMap, precision));
            }
            case MULTIPLY -> {
                BigDecimal prod = BigDecimal.ONE;
                for (SymbolicExpression c : expr.children) {
                    prod = prod.multiply(evaluateSymbolicExpression(c, varMap, precision));
                }
                return prod;
            }
            case DIVIDE -> {
                return evaluateSymbolicExpression(expr.children.get(0), varMap, precision)
                    .divide(evaluateSymbolicExpression(expr.children.get(1), varMap, precision), precision, BigDecimal.ROUND_HALF_UP);
            }
            case POWER -> {
                BigDecimal base = evaluateSymbolicExpression(expr.children.get(0), varMap, precision);
                BigDecimal exp = evaluateSymbolicExpression(expr.children.get(1), varMap, precision);
                double val = Math.pow(base.doubleValue(), exp.doubleValue());
                return new BigDecimal(val);
            }
            case LOG -> {
                BigDecimal arg = evaluateSymbolicExpression(expr.children.get(0), varMap, precision);
                double val = Math.log(arg.doubleValue());
                return new BigDecimal(val);
            }
            default -> throw new UnsupportedOperationException("Evaluation not implemented for " + expr.op);
        }
    }
}
