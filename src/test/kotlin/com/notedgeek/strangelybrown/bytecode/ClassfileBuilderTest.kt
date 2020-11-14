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

    @Test
    fun `build HelloWorldTestClass`() {
        val bytecode = buildBytecode {
            name("com.notedgeek.strangelybrown.bytecode.testClasses.java.HelloWorldTestClass")
            method {
                name("main")
                descriptor("([Ljava/lang/String;)V")
                access(ACC_PUBLIC or ACC_STATIC)
                code {
                    getStatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                    ldcString("Hello World!!")
                    invokeVirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V")
                    rtn()
                }
            }
        }
        val c = loadClassBuilderClass(bytecode)
        val o = c.getDeclaredConstructor().newInstance()
        println(o.javaClass)
        val m = c.getMethod("main", Array<String>::class.java)
        m.invoke(null, arrayOf("hello"))
    }

}