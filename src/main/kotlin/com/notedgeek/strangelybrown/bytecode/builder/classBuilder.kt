package com.notedgeek.strangelybrown.bytecode.builder

import com.notedgeek.strangelybrown.bytecode.*
import com.notedgeek.strangelybrown.bytecode.attribute.Attribute
import java.io.ByteArrayOutputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.util.*

fun buildClass(classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit) = classBuilder.apply(block)

fun buildBytecode(classBuilder: ClassBuilder = ClassBuilder(), block: ClassBuilder.() -> Unit = {}): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    classBuilder.apply(block).write(DataOutputStream(byteArrayOutputStream))
    return byteArrayOutputStream.toByteArray()
}

class ClassBuilder() {

    private var minorVersion = 0
    private var majorVersion = 52
    private val constantPool = ConstantPool()
    private var name = "package/ClassName"
    private var superclassName = "java/lang/Object"
    private var accessFlags = 0
    private val interfaceNames = LinkedList<String>()
    private val fields = LinkedList<Field>()
    private val methods = LinkedList<Method>()
    private val attributes = HashMap<String, Attribute>()

    private fun toClass(): Clazz {
        constantPool.ensureClass(name)
        constantPool.ensureClass(superclassName)
        return Clazz(
            minorVersion,
            majorVersion,
            constantPool,
            accessFlags,
            name,
            superclassName,
            interfaceNames,
            fields,
            methods,
            attributes
        )
    }

    fun write(dataOutput: DataOutput) {
        writeClassfile(toClass(), dataOutput)
    }

    fun name(name: String) {
        this.name = name.fromDotted()
    }

    fun superclassName(superclassName: String) {
        this.superclassName = superclassName.fromDotted()
    }

    fun implements(vararg interfaceNames: String) {
        for (interfaceName in interfaceNames.map(String::fromDotted)) {
            constantPool.ensureClass(interfaceName)
            this.interfaceNames.add(interfaceName)
        }
    }
}

private fun String.fromDotted() = this.replace('.', '/')