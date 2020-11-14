package com.notedgeek.strangelybrown.bytecode

import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

internal const val UTF8 = 1
internal const val INTEGER = 3
internal const val FLOAT = 4
internal const val LONG = 5
internal const val DOUBLE = 6
internal const val CLASS = 7
internal const val STRING = 8
internal const val FIELD_REF = 9
internal const val METHOD_REF = 10
internal const val NAME_AND_TYPE = 12

internal abstract class ConstantPoolEntry(var tag: Int) {

    var linked = false

    abstract fun loadFromDataInput(dataInput: DataInput)

    abstract fun writeToDataOutput(dataOutput: DataOutput)

    open fun link(constantPool: ConstantPool) {
        linked = true
    }

}

internal class ConstantUtf8 : ConstantPoolEntry(UTF8) {
    lateinit var value: String

    override fun loadFromDataInput(dataInput: DataInput) {
        value = dataInput.readUTF()
        logger.trace("CONSTANT_Utf8 with value \"{}\" loaded", value)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeUTF(value)
        logger.trace("CONSTANT_Utf8 with value \"{}\" written", value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantUtf8::class.java)
    }
}


internal class ConstantClass : ConstantPoolEntry(CLASS) {
    internal lateinit var name: String
    internal var nameIndex = 0

    override fun loadFromDataInput(dataInput: DataInput) {
        nameIndex = dataInput.readUnsignedShort()
        logger.trace("CONSTANT_Class with nameIndex {} loaded", nameIndex)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(nameIndex)
        logger.trace("CONSTANT_Class with nameIndex {} written", nameIndex)
    }

    override fun link(constantPool: ConstantPool) {
        if (!linked) {
            name = constantPool.getUtfValue(nameIndex)
            logger.trace("CONSTANT_Class linked with name \"{}\"", name)
            linked = true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantClass::class.java)
    }
}

internal class ConstantNameAndType : ConstantPoolEntry(NAME_AND_TYPE) {
    lateinit var name: String
    lateinit var descriptor: String
    var nameIndex = 0
    var descriptorIndex = 0

    override fun loadFromDataInput(dataInput: DataInput) {
        nameIndex = dataInput.readUnsignedShort()
        descriptorIndex = dataInput.readUnsignedShort()
        logger.trace("CONSTANT_NameAndType with nameIndex {} and descriptorIndex {} loaded", nameIndex, descriptorIndex)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(nameIndex)
        dataOutput.writeShort(descriptorIndex)
        logger.trace(
            "CONSTANT_NameAndType with nameIndex {} and descriptorIndex {} written",
            nameIndex,
            descriptorIndex
        )
    }

    override fun link(constantPool: ConstantPool) {
        if (!linked) {
            name = constantPool.getUtfValue(nameIndex)
            descriptor = constantPool.getUtfValue(descriptorIndex)
            logger.trace("CONSTANT_NameAndType linked with name \"{}\" and descriptor \"{}\"", name, descriptor)
            linked = true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantNameAndType::class.java)
    }
}


internal abstract class ConstantMethodOrFieldRef(tag: Int) : ConstantPoolEntry(tag) {
    var classIndex = 0
    var nameAndTypeIndex = 0
    var className: String? = null
    var name: String? = null
    var descriptor: String? = null

    fun expandNames(constantPool: ConstantPool) {
        val clazz = constantPool[classIndex] as ConstantClass
        clazz.link(constantPool)
        className = clazz.name
        val nameAndType: ConstantNameAndType = constantPool[nameAndTypeIndex] as ConstantNameAndType
        nameAndType.link(constantPool)
        name = nameAndType.name
        descriptor = nameAndType.descriptor
    }
}

internal class ConstantMethodRef : ConstantMethodOrFieldRef(METHOD_REF) {

    override fun loadFromDataInput(dataInput: DataInput) {
        classIndex = dataInput.readUnsignedShort()
        nameAndTypeIndex = dataInput.readUnsignedShort()
        logger.trace(
            "CONSTANT_MethodRef with classIndex {} and nameAndTypeIndex {} loaded",
            classIndex,
            nameAndTypeIndex
        )
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(classIndex)
        dataOutput.writeShort(nameAndTypeIndex)
        logger.trace(
            "CONSTANT_MethodRef with classIndex {} and nameAndTypeIndex {} written",
            classIndex,
            nameAndTypeIndex
        )
    }

    override fun link(constantPool: ConstantPool) {
        if (!linked) {
            expandNames(constantPool)
            logger.trace(
                "CONSTANT_MethodRef linked with className \"{}\", name \"{}\", and descriptor \"{}\"",
                className,
                name,
                descriptor
            )
            linked = true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantMethodRef::class.java)
    }
}

internal class ConstantFieldRef : ConstantMethodOrFieldRef(FIELD_REF) {

    override fun loadFromDataInput(dataInput: DataInput) {
        classIndex = dataInput.readUnsignedShort()
        nameAndTypeIndex = dataInput.readUnsignedShort()
        logger.trace(
            "CONSTANT_FieldRef with classIndex {} and nameAndTypeIndex {} loaded",
            classIndex,
            nameAndTypeIndex
        )
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(classIndex)
        dataOutput.writeShort(nameAndTypeIndex)
        logger.trace(
            "CONSTANT_FieldRef with classIndex {} and nameAndTypeIndex {} written",
            classIndex,
            nameAndTypeIndex
        )
    }

    override fun link(constantPool: ConstantPool) {
        if (!linked) {
            expandNames(constantPool)
            logger.trace(
                "CONSTANT_FieldRef linked with className \"{}\", name \"{}\", and descriptor \"{}\"",
                className,
                name,
                descriptor
            )
            linked = true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantFieldRef::class.java)
    }
}

internal class ConstantString : ConstantPoolEntry(STRING) {
    private var utf8Index = 0
    private var utf8Value: String? = null

    override fun loadFromDataInput(dataInput: DataInput) {
        utf8Index = dataInput.readUnsignedShort()
        logger.trace("CONSTANT_String with utf8Index {} loaded", utf8Index)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(utf8Index)
        logger.trace("CONSTANT_String with value {} written", utf8Value)
    }

    override fun link(constantPool: ConstantPool) {
        if (!linked) {
            utf8Value = constantPool.getUtfValue(utf8Index)
            logger.trace("CONSTANT_String linked with value \"{}\"", utf8Value)
            linked = true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantString::class.java)
    }
}

internal class ConstantInteger : ConstantPoolEntry(INTEGER) {
    private var value = 0

    override fun loadFromDataInput(dataInput: DataInput) {
        value = dataInput.readInt()
        logger.trace("CONSTANT_Integer : {} loaded", value)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeInt(value)
        logger.trace("CONSTANT_Integer : {} written", value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantInteger::class.java)
    }
}

internal class ConstantFloat : ConstantPoolEntry(FLOAT) {
    private var value = 0f

    override fun loadFromDataInput(dataInput: DataInput) {
        value = dataInput.readFloat()
        logger.trace("CONSTANT_Float : {} loaded", value)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeFloat(value)
        logger.trace("CONSTANT_Float : {} written", value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantFloat::class.java)
    }
}

internal class ConstantLong : ConstantPoolEntry(LONG) {
    private var value: Long = 0

    override fun loadFromDataInput(dataInput: DataInput) {
        value = dataInput.readLong()
        logger.trace("CONSTANT_Long : {} loaded", value)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeLong(value)
        logger.trace("CONSTANT_Long : {} written", value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantLong::class.java)
    }
}

internal class ConstantDouble : ConstantPoolEntry(DOUBLE) {
    private var value = 0.0

    override fun loadFromDataInput(dataInput: DataInput) {
        value = dataInput.readDouble()
        logger.trace("CONSTANT_Double : {} loaded", value)
    }

    override fun writeToDataOutput(dataOutput: DataOutput) {
        dataOutput.writeDouble(value)
        logger.trace("CONSTANT_Double : {} written", value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConstantDouble::class.java)
    }
}

