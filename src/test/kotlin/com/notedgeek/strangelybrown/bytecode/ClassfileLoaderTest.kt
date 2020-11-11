package com.notedgeek.strangelybrown.bytecode

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.function.Supplier

private val loader = RoundTripClassLoader("com/notedgeek/strangelybrown/bytecode/testClasses/java")

class ClassfileLoaderTest {

    @Test
    fun `load EmptyTestClass`() {
        val c = loader.loadClass("EmptyTestClass")
        val o = c.getDeclaredConstructor().newInstance()
    }

    @Test
    fun `load Greeter default constructor`() {
        val c = loader.loadClass("Greeter")
        val o = c.getDeclaredConstructor().newInstance()
        val m = c.getMethod("greet", String::class.java)
        assertThat(m.invoke(o,"Butch")).isEqualTo("Hello Butch!")
    }

    @Test
    fun `load Greeter constructor with argument`() {
        val c = loader.loadClass("Greeter")
        val o = c.getDeclaredConstructor(String::class.java).newInstance("Greetings")
        val m = c.getMethod("greet", String::class.java)
        assertThat(m.invoke(o,"Butch")).isEqualTo("Greetings Butch!")
    }

    @Test
    fun `load StringSupplier`() {
        val c = loader.loadClass("StringSupplier")
        val s = "This ia a string."
        val o = c.getDeclaredConstructor(String::class.java).newInstance(s) as Supplier<String>
        assertThat(o.get()).isEqualTo(s)
    }

}