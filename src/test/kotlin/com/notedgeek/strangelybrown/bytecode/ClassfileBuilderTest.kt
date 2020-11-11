package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.builder.buildBytecode
import org.junit.jupiter.api.Test

private val loader = RoundTripClassLoader("com/notedgeek/strangelybrown/bytecode/testClasses/java")

class ClassfileBuilderTest {

    @Test
    fun `build EmptyTestClass`() {
        val bytecode = buildBytecode {
            name("com.notedgeek.strangelybrown.bytecode.testClasses.java.EmptyTestClass")
            implements("java.io.Serializable", "java.lang.Runnable")
        }
        val c = loadClassBuilderClass(bytecode)
        // val o = c.getDeclaredConstructor().newInstance()
    }

}