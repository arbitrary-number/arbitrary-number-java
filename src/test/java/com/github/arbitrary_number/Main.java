package com.github.arbitrary_number;

public class Main {
    public static void main(String[] args) {
        ArbitraryNumber x = new ArbitraryNumber();
        x.addTerm(1, 1, 3);  // 1/3

        ArbitraryNumber y = new ArbitraryNumber();
        y.addTerm(1, 1, 2);  // 1/2

        ArbitraryNumber z = x.add(y);
        System.out.println("x = " + x);
        System.out.println("y = " + y);
        System.out.println("z = x + y = " + z);
        System.out.println("Decimal approximation of z: " + z.evaluateToDecimal(30));

        ArbitraryNumber p = x.multiply(y);
        System.out.println("p = x * y = " + p);
        System.out.println("Decimal of p: " + p.evaluateToDecimal(30));
    }
}
