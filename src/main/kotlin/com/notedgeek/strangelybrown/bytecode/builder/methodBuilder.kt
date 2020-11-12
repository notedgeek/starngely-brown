package com.notedgeek.strangelybrown.bytecode.builder

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import com.notedgeek.strangelybrown.bytecode.Method
import com.notedgeek.strangelybrown.bytecode.attribute.Attribute
import com.notedgeek.strangelybrown.bytecode.attribute.CodeAttribute

internal fun buildMethod(constantPool: ConstantPool, block: MethodBuilder.() -> Unit) =
    MethodBuilder(constantPool).apply(block).toMethod()

class MethodBuilder internal constructor(private val constantPool: ConstantPool) {
    private var accessFlags = 0
    private var name = "aMethod"
    private var descriptor = "()V"
    private var attributes = HashMap<String, Attribute>()
    private val codeAttribute = CodeAttribute()

    init {
        codeAttribute.maxLocals = 1
        codeAttribute.code = byteArrayOf(0xb1.toByte())
    }

    internal fun toMethod(): Method {
        constantPool.ensureConstantUtfString(name)
        constantPool.ensureConstantUtfString(descriptor)
        attributes["Code"] = codeAttribute
        for (key in attributes.keys) {
            constantPool.ensureConstantUtfString(key)
        }
        return Method(accessFlags, name, descriptor, attributes)
    }
}