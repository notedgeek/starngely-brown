package com.notedgeek.strangelybrown.bytecode.testClasses.java;

@SuppressWarnings("unused")
public class Greeter {

    private final String greeting;

    public Greeter() {
        this("Hello");
    }

    public Greeter(String greeting) {
        this.greeting = greeting;
    }

    public String greet(String name) {
        return greeting + " " + name + "!";
    }
}
