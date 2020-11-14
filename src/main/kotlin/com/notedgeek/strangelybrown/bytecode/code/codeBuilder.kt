package com.notedgeek.strangelybrown.bytecode.code

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import com.notedgeek.strangelybrown.bytecode.ScopeMarker
import com.notedgeek.strangelybrown.bytecode.TypeParser
import com.notedgeek.strangelybrown.bytecode.attribute.CodeAttribute
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

internal fun buildCode(constantPool: ConstantPool, maxLocals: Int, block: CodeBuilder.() -> Unit) =
    CodeBuilder(constantPool, maxLocals).apply(block).toCodeAttribute()

@ScopeMarker
class CodeBuilder internal constructor(var constantPool: ConstantPool, var maxLocals: Int) {

    var maxStack = 0
    var stack = 0
    var byteArrayOutputStream = ByteArrayOutputStream()
    var dataOutput = DataOutputStream(byteArrayOutputStream)

    internal fun toCodeAttribute(): CodeAttribute {
        val result = CodeAttribute()
        result.maxLocals = maxLocals
        result.maxStack = maxStack
        result.code = byteArrayOutputStream.toByteArray()
        return result
    }

    fun methodRef(classname: String, name: String, descriptor: String) =
        constantPool.ensureMethodRef(classname, name, descriptor)

    fun fieldRef(classname: String, name: String, descriptor: String) =
        constantPool.ensureFieldRef(classname, name, descriptor)

    fun constantString(string: String) = constantPool.ensureConstantString(string)

    fun aLoad(i: Int) {
        if (i < 4) {
            addByte(ALOAD_0 + i)
        } else {
            addByte(ALOAD, i)
        }
        incStack()
    }

    fun invokeSpecial(classname: String, name: String, descriptor: String) {
        instruction(INVOKE_SPECIAL)
        cpIndex(methodRef(classname, name, descriptor))
        decStack(1 + stackCountForDescriptor(descriptor))
    }

    fun invokeVirtual(classname: String, name: String, descriptor: String) {
        instruction(INVOKE_VIRTUAL)
        cpIndex(methodRef(classname, name, descriptor))
        decStack(1 + stackCountForDescriptor(descriptor))
    }

    fun getStatic(classname: String, name: String, descriptor: String) {
        getStatic(fieldRef(classname, name, descriptor))
    }

    fun getStatic(i: Int) {
        instruction(GET_STATIC)
        addShort(i)
        incStack()
    }

    fun ldcString(s: String) {
        val index = constantString(s)
        if (index > 255) {
            throw Exception("index too high - implement wide")
        }
        ldc(index)
    }

    fun ldc(i: Int) {
        instruction(LDC)
        addByte(i)
        incStack()
    }

    fun rtn() = instruction(RETURN)

    private fun addByte(vararg ints: Int) {
        for (int in ints) {
            dataOutput.writeByte(int)
        }
    }

    private fun addShort(vararg ints: Int) {
        for (int in ints) {
            dataOutput.writeShort(int)
        }
    }

    private fun instruction(vararg ints: Int) = addByte(*ints)

    private fun cpIndex(int: Int) = dataOutput.writeShort(int)

    private fun incStack(n: Int = 1) {
        stack += n
        if (stack > maxStack) {
            maxStack = stack
        }
    }

    private fun decStack(n: Int = 1) {
        stack -= n
    }

    private fun stackCountForDescriptor(descriptor: String): Int {
        val paramTypes = TypeParser(descriptor).methodType().paramList
        var result = 0
        for (paramType in paramTypes) {
            result += paramType.width
        }
        return result
    }
}