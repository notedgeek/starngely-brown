package com.notedgeek.strangelybrown.bytecode

import java.util.*

internal sealed class Type

internal sealed class FieldType(val width: Int = 1) : Type()

internal open class BaseType(val char: Char, width: Int = 1) : FieldType(width) {

    companion object {
        fun fromChar(char: Char): BaseType = when (char) {
            'B' -> TByte
            'C' -> TChar
            'D' -> TDouble
            'F' -> TFloat
            'I' -> TInt
            'J' -> TLong
            'S' -> TShort
            'Z' -> TBoolean
            else -> throw Exception("no base type for char $char")
        }
    }

    override fun toString() = "$char"
}

internal object TByte : BaseType('B') {}
internal object TChar : BaseType('C')
internal object TDouble : BaseType('D', 2)
internal object TFloat : BaseType('F')
internal object TInt : BaseType('I')
internal object TLong : BaseType('J', 2)
internal object TShort : BaseType('S')
internal object TBoolean : BaseType('Z')

internal class ObjectType(val className: String) : FieldType() {
    override fun toString() = "L$className;"
}

internal class ArrayType(val type: FieldType) : FieldType() {
    override fun toString() = "[${type}"
}

internal object VoidType : Type() {
    override fun toString() = "V"
}

internal class MethodType(val returnType: Type, vararg params: FieldType) {
    val paramList = params.asList()

    override fun toString() = StringBuilder().apply {
        append('(')
        for (param in paramList) {
            append(param)
        }
        append(')')
        append(returnType)
    }.toString()
}


private class TypeParser(string: String) {

    companion object {
        fun field(string: String) = TypeParser(string).fieldType()
        fun method(string: String) = TypeParser(string).methodType()
    }

    val chars = LinkedList<Char>()

    init {
        chars.addAll(string.toCharArray().toList())
    }

    fun consume(c: Char) {
        if (chars.first != c) {
            throw Exception("expected $c, got ${chars.first}")
        }
        chars.removeFirst()
    }

    fun stringUntilSemiColon(): String {
        val result = StringBuilder()
        while (true) {
            if (chars.size < 1) {
                throw Exception("EOF reading until ;")
            }
            val c = chars.removeFirst()
            if (c == ';') {
                break
            }
            result.append(c)
        }
        return result.toString()
    }

    fun fieldType(): FieldType {
        return when (chars.first) {
            '[' -> arrayType()
            'L' -> objectType()
            else -> baseType()
        }
    }

    fun baseType(): FieldType = BaseType.fromChar(chars.removeFirst())

    fun objectType(): ObjectType {
        consume('L')
        return ObjectType(stringUntilSemiColon())
    }

    fun arrayType(): ArrayType {
        consume('[')
        return ArrayType(fieldType())
    }

    fun returnType(): Type {
        return when (chars.first) {
            'V' -> VoidType
            else -> fieldType()
        }
    }

    fun methodType(): MethodType {
        val paramTypes = LinkedList<FieldType>()
        consume('(')
        while (true) {
            when (chars.first) {
                ')' -> {
                    consume(')')
                    break
                }
                else -> paramTypes.add(fieldType())
            }
        }
        return MethodType(returnType(), *(paramTypes.toTypedArray()))
    }
}
