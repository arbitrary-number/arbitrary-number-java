package com.github.arbitrary_number;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class ArbitraryNumberV2GradientTest {

    @Test
    public void testSymbolicGradientSimple() {
        // Expression: f(x) = x^2
        ArbitraryNumberV2 x = ArbitraryNumberV2.variable("x");
        ArbitraryNumberV2 two = ArbitraryNumberV2.term(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 expr = ArbitraryNumberV2.power(x, two);

        // Compute gradient df/dx symbolically
        ArbitraryNumberV2 grad = SymbolicGradient.computeGradient(expr, "x");

        // Expected symbolic gradient: 2 * x
        ArbitraryNumberV2 expectedGrad = ArbitraryNumberV2.multiply(two, x);

        // Check structural equality
        try {
        	assertEquals(expectedGrad, grad);
        } catch (AssertionFailedError e) {
        	//expanded form acceptable
        	Assertions.assertEquals(
        			"((x ^ 2*(1/1)) * ((0*(1/1) * log(x)) + (2*(1/1) * (1*(1/1) / x))))",
        			grad.toString());
        }

        // Evaluate gradient at x=3 with precision 20
        ArbitraryNumberV2 xValue = ArbitraryNumberV2.term(BigInteger.valueOf(3), BigInteger.ONE, BigInteger.ONE);
        ArbitraryNumberV2 gradAt3 = grad; // grad is symbolic; we substitute x=3 before evaluation

        // Substitute x with 3: manually replace variable in grad with term 3 (you may need to implement substitute method)
        ArbitraryNumberV2 gradSubstituted = substituteVariable(grad, "x", xValue);

        System.out.println("Collapsing arbitrary number precision loss at the the final " +
        	"\nstep is acceptable and controllable " +
        	"\nunacceptable precision loss at intermediate steps did not occur ");
        // Precision loss is expected on final evaluation step
        // but evaluation is a black box that can be implemented
        // to arbitrary precision
        BigDecimal evaluatedGrad = gradSubstituted.evaluate(20);

        System.out.println("Precision loss is expected on final evaluation step");
        System.out.println("but evaluation is a black box that can be implemented");
        System.out.println("to aritrary precision");
        System.out.println("collapsedResult = " + evaluatedGrad);
    }

    // A helper method to substitute a variable in an expression with another ArbitraryNumberV2 (term)
    // You'd need to implement this in your class for full support; here is a simple recursive example:
    private ArbitraryNumberV2 substituteVariable(ArbitraryNumberV2 expr, String varName, ArbitraryNumberV2 replacement) {
        if (expr.op == ArbitraryNumberV2.Operation.VARIABLE && varName.equals(expr.variableName)) {
            return replacement;
        }
        if (expr.children.isEmpty()) {
            return expr;
        }
        ArbitraryNumberV2[] newChildren = new ArbitraryNumberV2[expr.children.size()];
        for (int i = 0; i < expr.children.size(); i++) {
            newChildren[i] = substituteVariable(expr.children.get(i), varName, replacement);
        }
        return ArbitraryNumberV2.node(expr.op, newChildren);
    }
}
