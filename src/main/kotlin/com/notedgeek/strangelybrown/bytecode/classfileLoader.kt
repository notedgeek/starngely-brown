package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.attribute.loadAttributes
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.lang.Integer.toHexString
import java.util.*

private val logger = LoggerFactory.getLogger("classfileLoader")

internal fun loadClassFile(dataInput: DataInput): Clazz {
    logger.debug("loading classfile...")
    checkMagicNumber(dataInput)
    logger.trace("magic number ok")
    val minorVersion = dataInput.readUnsignedShort()
    logger.trace("minor version: $minorVersion")
    val majorVersion = dataInput.readUnsignedShort()
    logger.trace("major version: $majorVersion")
    val constantPool = ConstantPool.loadAndLink(dataInput)
    val accessFlags = dataInput.readUnsignedShort()
    logger.trace("access flags: ${Integer.toHexString(accessFlags)}")
    val className = constantPool.getClassName(dataInput)
    logger.trace("class name $className")
    val superclassName = constantPool.getClassName(dataInput)
    logger.trace("superclass name $superclassName")
    val interfaceNames = loadInterfaces(dataInput, constantPool)
    val fields = loadFields(dataInput, constantPool)
    val methods = loadMethods(dataInput, constantPool)
    val attributes = loadAttributes(dataInput, constantPool)
    logger.debug("classfile loaded for $className")
    return Clazz(
        minorVersion,
        majorVersion,
        constantPool,
        accessFlags,
        className,
        superclassName,
        interfaceNames,
        fields,
        methods,
        attributes
    )
}

private fun checkMagicNumber(dataInput: DataInput) {
    val magicNumber = dataInput.readInt()
    if (magicNumber != MAGIC_NUMBER) {
        throw Exception("invalid magic number: ${toHexString(magicNumber)} - expected ${toHexString(MAGIC_NUMBER)}")
    }
}

private fun loadInterfaces(di: DataInput, constantPool: ConstantPool): List<String> {
    logger.trace("loading interfaces")
    val interfaceCount = di.readUnsignedShort()
    val interfaceNames = ArrayList<String>(interfaceCount)
    if (interfaceCount > 0) {
        logger.trace("interface count : {}", interfaceCount)
        for (i in 0 until interfaceCount) {
            val interfaceName = constantPool.getClassName(di)
            interfaceNames.add(interfaceName)
            logger.debug(interfaceName)
        }
    } else {
        logger.trace("no interfaces")
    }
    return interfaceNames
}

private fun loadFields(di: DataInput, constantPool: ConstantPool): List<Field> {
    logger.trace("loading fields")
    val fieldCount: Int = di.readUnsignedShort()
    val fields = ArrayList<Field>(fieldCount)
    if (fieldCount > 0) {
        logger.debug("field count : $fieldCount")
        for (i in 0 until fieldCount) {
            fields.add(loadField(di, constantPool))
        }
    } else {
        logger.trace("no fields")
    }
    return fields
}

private fun loadField(dataInput: DataInput, constantPool: ConstantPool): Field {
    val accessFlags = dataInput.readUnsignedShort()
    val name = constantPool.getUtfValue(dataInput)
    val descriptor = constantPool.getUtfValue(dataInput)
    logger.trace("loading field with name \"{}\", and descriptor \"{}\"", name, descriptor)
    logger.trace("loading field attributes")
    val attributes = loadAttributes(dataInput, constantPool)
    return Field(accessFlags, name, descriptor, attributes)
}

private fun loadMethods(di: DataInput, constantPool: ConstantPool): List<Method> {
    logger.trace("loading methods")
    val methodCount: Int = di.readUnsignedShort()
    val methods = ArrayList<Method>(methodCount)
    if (methodCount > 0) {
        logger.trace("method count : $methodCount")
        for (i in 0 until methodCount) {
            methods.add(loadMethod(di, constantPool))
        }
    } else {
        logger.trace("no methods")
    }
    return methods
}

private fun loadMethod(dataInput: DataInput, constantPool: ConstantPool): Method {
    val accessFlags = dataInput.readUnsignedShort()
    val name = constantPool.getUtfValue(dataInput)
    val descriptor = constantPool.getUtfValue(dataInput)
    logger.trace("loading method with name \"{}\", and descriptor \"{}\"", name, descriptor)
    logger.trace("loading method attributes")
    val attributes = loadAttributes(dataInput, constantPool)
    return Method(accessFlags, name, descriptor, attributes)
}


