package com.notedgeek.strangelybrown.bytecode.code

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import com.notedgeek.strangelybrown.bytecode.attribute.CodeAttribute

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

internal fun buildCode(constantPool: ConstantPool, maxLocals: Int, block: CodeBuilder.() -> Unit) =
    CodeBuilder(constantPool, maxLocals).apply(block).toCodeAttribute()

class CodeBuilder internal constructor(var constantPool: ConstantPool, var maxLocals: Int) {

    var maxStack = 0
    var byteArrayOutputStream = ByteArrayOutputStream()
    var dataOutput = DataOutputStream(byteArrayOutputStream)

    internal fun toCodeAttribute(): CodeAttribute {
        val result = CodeAttribute()
        result.maxLocals = maxLocals
        result.maxStack = maxStack
        result.code = byteArrayOutputStream.toByteArray()
        return result
    }

    fun aLoad(i: Int) {
        if (i < 4) {
            addByte(ALOAD_0 + i)
        } else {
            addByte(ALOAD, i)
        }
        maxStack++
    }

    fun invokeSpecial(classname: String, name: String, descriptor: String) {
        invokeSpecial(constantPool.ensureMethodRef(classname, name, descriptor))
    }

    fun invokeSpecial(i: Int) {
        instruction(INVOKE_SPECIAL)
        cpIndex(i)
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
}