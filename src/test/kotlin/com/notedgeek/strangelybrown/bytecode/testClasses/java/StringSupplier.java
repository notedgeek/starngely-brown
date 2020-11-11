package com.notedgeek.strangelybrown.bytecode.testClasses.java;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class StringSupplier implements Supplier<String> {

    private final String string;

    public StringSupplier(String string) {
        this.string = string;
    }

    @Override
    public String get() {
        return string;
    }

}
