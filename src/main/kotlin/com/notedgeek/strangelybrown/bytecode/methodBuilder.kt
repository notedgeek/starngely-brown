package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.attribute.Attribute
import com.notedgeek.strangelybrown.bytecode.attribute.CodeAttribute
import com.notedgeek.strangelybrown.bytecode.code.CodeBuilder
import com.notedgeek.strangelybrown.bytecode.code.buildCode

internal fun buildMethod(constantPool: ConstantPool, block: MethodBuilder.() -> Unit) =
    MethodBuilder(constantPool).apply(block).toMethod()

@ScopeMarker
class MethodBuilder internal constructor(private val constantPool: ConstantPool) {
    private var accessFlags = ACC_PUBLIC
    private var name = "aMethod"
    private var descriptor = "()V"
    private var methodType = TypeParser.method(descriptor)
    private var attributes = HashMap<String, Attribute>()
    private var codeAttribute = CodeAttribute()

    fun name(name: String) {
        this.name = name
    }

    fun descriptor(descriptor: String) {
        this.descriptor = descriptor
        this.methodType = TypeParser.method(descriptor)
    }

    fun code(block: CodeBuilder.() -> Unit) {
        var maxLocals = 0
        if (accessFlags and ACC_STATIC == 0) {
            maxLocals++
        }
        for (param in methodType.paramList) {
            maxLocals += param.width
        }
        codeAttribute = buildCode(constantPool, maxLocals, block)
    }

    fun access(accessFlags: Int) {
        this.accessFlags = accessFlags
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