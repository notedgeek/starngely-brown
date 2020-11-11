package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.attribute.Attribute

private const val ACC_PUBLIC = 0x001
private const val ACC_PRIVATE = 0x0002
private const val ACC_PROTECTED = 0x0004
private const val ACC_STATIC = 0x0008
private const val ACC_FINAL = 0x010
private const val ACC_SUPER = 0x020
private const val ACC_SYNCHRONIZED = 0x0020
private const val ACC_BRIDGE = 0x0040
private const val ACC_VOLATILE = 0x0040
private const val ACC_VARARGS = 0x0080
private const val ACC_TRANSIENT = 0x0080
private const val ACC_STRICT = 0x0800
private const val ACC_NATIVE = 0x0100
private const val ACC_INTERFACE = 0x200
private const val ACC_ABSTRACT = 0x400
private const val ACC_SYNTHETIC = 0x1000
private const val ACC_ANNOTATION = 0x2000
private const val ACC_ENUM = 0x4000

internal abstract class AccessControlled(val accessBitmap: Int) {
    val isPublic: Boolean
        get() = testAccessFlag(ACC_PUBLIC)
    val isFinal: Boolean
        get() = testAccessFlag(ACC_FINAL)
    val isSynthetic: Boolean
        get() = testAccessFlag(ACC_SYNTHETIC)

    fun testAccessFlag(mask: Int): Boolean {
        return accessBitmap and mask != 0
    }
}

internal abstract class Member(accessBitmap: Int) : AccessControlled(accessBitmap) {
    lateinit var name: String
    lateinit var descriptor: String
    lateinit var attributes: Map<String, Attribute>

    val isPrivate: Boolean
        get() = testAccessFlag(ACC_PRIVATE)
    val isProtected: Boolean
        get() = testAccessFlag(ACC_PROTECTED)
    val isStatic: Boolean
        get() = testAccessFlag(ACC_STATIC)
}

internal class Field(accessBitmap: Int) : Member(accessBitmap) {
    val isVolatile: Boolean
        get() = testAccessFlag(ACC_VOLATILE)
    val isTransient: Boolean
        get() = testAccessFlag(ACC_TRANSIENT)
    val isEnum: Boolean
        get() = testAccessFlag(ACC_ENUM)
}

internal class Method(accessBitmap: Int) : Member(accessBitmap) {
    val isSynchronized: Boolean
        get() = testAccessFlag(ACC_SYNCHRONIZED)
    val isBridge: Boolean
        get() = testAccessFlag(ACC_BRIDGE)
    val isVarargs: Boolean
        get() = testAccessFlag(ACC_VARARGS)
    val isNative: Boolean
        get() = testAccessFlag(ACC_NATIVE)
    val isAbstract: Boolean
        get() = testAccessFlag(ACC_ABSTRACT)
    val isStrict: Boolean
        get() = testAccessFlag(ACC_STRICT)
}

internal class Clazz(
    val minorVersion: Int,
    val majorVersion: Int,
    val constantPool: ConstantPool,
    accessBitmap: Int,
    val name: String,
    val superclassName: String,
    val interfaceNames: List<String>,
    val fields: List<Field>,
    val methods: List<Method>,
    val attributes: Map<String, Attribute>
) : AccessControlled(accessBitmap) {

    val isSuper: Boolean
        get() = testAccessFlag(ACC_SUPER)
    val isInterface: Boolean
        get() = testAccessFlag(ACC_INTERFACE)
    val isAbstract: Boolean
        get() = testAccessFlag(ACC_ABSTRACT)
    val isAnnotation: Boolean
        get() = testAccessFlag(ACC_ANNOTATION)
    val isEnum: Boolean
        get() = testAccessFlag(ACC_ENUM)
}
