package com.github.arbitrary_number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ArbitraryNumber {

    public static class Term {
        public BigInteger c; // Scalar
        public BigInteger a; // Numerator
        public BigInteger b; // Denominator

        public Term(BigInteger c, BigInteger a, BigInteger b) {
            if (b.equals(BigInteger.ZERO)) throw new IllegalArgumentException("Denominator cannot be zero.");
            this.c = c;
            this.a = a;
            this.b = b;
        }

        public Term(long c, long a, long b) {
            this(BigInteger.valueOf(c), BigInteger.valueOf(a), BigInteger.valueOf(b));
        }

        public BigInteger evaluateNumerator() {
            return c.multiply(a);
        }

        public BigInteger evaluateDenominator() {
            return b;
        }

        public String toString() {
            return c + "*(" + a + "/" + b + ")";
        }
    }

    private final List<Term> terms;

    public ArbitraryNumber() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(long c, long a, long b) {
        terms.add(new Term(c, a, b));
    }

    public void addTerm(BigInteger c, BigInteger a, BigInteger b) {
        terms.add(new Term(c, a, b));
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public List<Term> getTerms() {
        return terms;
    }

    public ArbitraryNumber add(ArbitraryNumber other) {
        ArbitraryNumber result = new ArbitraryNumber();
        result.terms.addAll(this.terms);
        result.terms.addAll(other.terms);
        return result;
    }

    public ArbitraryNumber multiply(ArbitraryNumber other) {
        ArbitraryNumber result = new ArbitraryNumber();
        for (Term t1 : this.terms) {
            for (Term t2 : other.terms) {
                BigInteger newC = t1.c.multiply(t2.c);
                BigInteger newA = t1.a.multiply(t2.a);
                BigInteger newB = t1.b.multiply(t2.b);
                result.addTerm(newC, newA, newB);
            }
        }
        return result;
    }

    public BigDecimal evaluateToDecimal(int precision) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Term t : terms) {
            BigDecimal numerator = new BigDecimal(t.evaluateNumerator());
            BigDecimal denominator = new BigDecimal(t.evaluateDenominator());
            sum = sum.add(numerator.divide(denominator, precision, RoundingMode.HALF_UP));
        }
        return sum;
    }

    public ArbitraryNumber divideBy(ArbitraryNumber other) {
        if (other.terms.isEmpty()) {
            throw new ArithmeticException("Cannot divide by zero (empty number).");
        }
        if (other.terms.size() != 1) {
            throw new UnsupportedOperationException("Only division by single-term numbers is supported for now.");
        }

        Term divisor = other.terms.get(0);
        if (divisor.a.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Cannot divide by zero-valued term.");
        }

        ArbitraryNumber result = new ArbitraryNumber();

        for (Term t : this.terms) {
            // (c1 * a1 / b1) / (c2 * a2 / b2)
            // = (c1 * a1 * b2) / (b1 * c2 * a2)
            BigInteger newNumerator = t.c.multiply(t.a).multiply(divisor.b);
            BigInteger newDenominator = t.b.multiply(divisor.c).multiply(divisor.a);

            if (newDenominator.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Division by zero during term computation.");
            }

            result.addTerm(BigInteger.ONE, newNumerator, newDenominator);
        }

        return result;
    }


    public String toString() {
        if (terms.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            if (i > 0) sb.append(" + ");
            sb.append(terms.get(i));
        }
        return sb.toString();
    }
}
