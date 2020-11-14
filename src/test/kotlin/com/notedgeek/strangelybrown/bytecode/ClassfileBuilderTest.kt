package com.notedgeek.strangelybrown.bytecode

import org.junit.jupiter.api.Test

class ClassfileBuilderTest {

    @Test
    fun `build EmptyTestClass`() {
        val bytecode = buildBytecode {
            name("com.notedgeek.strangelybrown.bytecode.testClasses.java.EmptyTestClass")
        }
        val c = loadClassBuilderClass(bytecode)
        val o = c.getDeclaredConstructor().newInstance()
        println(o.javaClass)
    }

}