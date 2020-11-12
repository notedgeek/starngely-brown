package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataOutput
import java.io.DataOutputStream

private val logger = LoggerFactory.getLogger("attributeLoader")

internal abstract class Attribute {

    lateinit var name: String

    open fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        throw RuntimeException("loadFromDataInput not implemented for $name attribute")
    }

    open fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        throw RuntimeException("writeContentsToDataOutput not implemented for $name attribute")
    }

    fun writeToDataOutputWithNameAndLength(dataOutput: DataOutput, constantPool: ConstantPool) {
        val baos = ByteArrayOutputStream()
        val byteArrayDataOutput: DataOutput = DataOutputStream(baos)
        writeContentsToDataOutput(byteArrayDataOutput, constantPool)
        val byteArray = baos.toByteArray()
        dataOutput.writeShort(constantPool.getIndexOfUtfString(name))
        dataOutput.writeInt(byteArray.size)
        dataOutput.write(byteArray)
    }

}

internal fun loadAttributes(dataInput: DataInput, constantPool: ConstantPool): Map<String, Attribute> {
    val attributeCount: Int = dataInput.readUnsignedShort()
    logger.trace("attribute count : $attributeCount")
    val attributes = HashMap<String, Attribute>()
    for (i in 0 until attributeCount) {
        val attribute = loadAttribute(dataInput, constantPool)
        attributes[attribute.name] = attribute
    }
    return attributes
}

internal fun writeAttributes(attributes: Map<String, Attribute>, dataOutput: DataOutput, constantPool: ConstantPool) {
    val attributeCount = attributes.size
    dataOutput.writeShort(attributeCount)
    if (attributeCount == 0) {
        logger.trace("no attributes")
        return
    }
    logger.debug("attribute count : {}", attributeCount)
    for (attribute in attributes.values) {
        logger.trace("writing attribute \"{}\"", attribute.name)
        attribute.writeToDataOutputWithNameAndLength(dataOutput, constantPool)
        logger.trace("attribute \"{}\" written", attribute.name)
    }
}


private fun loadAttribute(dataInput: DataInput, constantPool: ConstantPool): Attribute {
    val attributeName: String = constantPool.getUtfValue(dataInput)
    logger.trace("loading attribute \"$attributeName\"")
    val attribute: Attribute = when (attributeName) {
        "ConstantValue" -> ConstantValueAttribute()
        "Code" -> CodeAttribute()
        "LineNumberTable" -> LineNumberTableAttribute()
        "SourceFile" -> SourceFileAttribute()
        "LocalVariableTable" -> LocalVariableTableAttribute()
        "Signature" -> SignatureAttribute()
        else -> throw RuntimeException("attribute name $attributeName not supported")
    }
    attribute.name = attributeName
    dataInput.readInt() // throw away attribute length
    attribute.loadFromDataInput(dataInput, constantPool)
    logger.trace("attribute \"$attributeName\" loaded")
    return attribute
}


