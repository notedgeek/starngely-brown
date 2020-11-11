package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.builder.buildClass
import org.junit.jupiter.api.Test

private val loader = RoundTripClassLoader("com/notedgeek/strangelybrown/bytecode/testClasses/java")

class ClassfileBuilderTest {

    @Test
    fun `build EmptyTestClass`() {
        val c = loadClassBuilderClass(buildClass {
            name("com.notedgeek.strangelybrown.bytecode.testClasses.java.EmptyTestClass")
            implements("java.io.Serializable", "java.lang.Runnable")
        })
        // val o = c.getDeclaredConstructor().newInstance()
    }

}