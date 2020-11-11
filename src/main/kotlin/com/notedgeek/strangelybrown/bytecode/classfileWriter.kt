package com.notedgeek.strangelybrown.bytecode

import com.notedgeek.strangelybrown.bytecode.attribute.writeAttributes
import org.slf4j.LoggerFactory
import java.io.DataOutput

private val logger = LoggerFactory.getLogger("classfileWriter")

internal fun writeClassfile(clazz: Clazz, dataOutput: DataOutput) {
    logger.debug("writing classfile {}...", clazz.name)
    dataOutput.writeInt(MAGIC_NUMBER)
    dataOutput.writeShort(clazz.minorVersion)
    dataOutput.writeShort(clazz.majorVersion)
    val constantPool = clazz.constantPool
    constantPool.writeToDataOutput(dataOutput)
    dataOutput.writeShort(clazz.accessBitmap)
    dataOutput.writeShort(constantPool.getIndexOfClassName(clazz.name))
    dataOutput.writeShort(constantPool.getIndexOfClassName(clazz.superclassName))
    writeInterfaces(clazz, dataOutput)
    writeFields(clazz.fields, dataOutput, constantPool)
    writeMethods(clazz.methods, dataOutput, constantPool)
    writeAttributes(clazz.attributes, dataOutput, constantPool)
    logger.debug("classfile {} written", clazz.name)
}

private fun writeInterfaces(clazz: Clazz, dataOutput: DataOutput) {
    logger.trace("writing interfaces")
    val interfaceCount: Int = clazz.interfaceNames.size
    dataOutput.writeShort(interfaceCount)
    if (interfaceCount > 0) {
        logger.trace("interface count : {}", interfaceCount)
        for (interfaceName in clazz.interfaceNames) {
            logger.debug(interfaceName)
            dataOutput.writeShort(clazz.constantPool.getIndexOfClassName(interfaceName))
        }
    } else {
        logger.trace("no interfaces")
    }
}

private fun writeFields(fields: List<Field>, dataOutput: DataOutput, constantPool: ConstantPool) {
    logger.trace("writing fields")
    val fieldCount: Int = fields.size
    dataOutput.writeShort(fieldCount)
    if (fieldCount > 0) {
        logger.trace("field count : {}", fieldCount)
        for (field in fields) {
            writeField(field, dataOutput, constantPool)
        }
    } else {
        logger.trace("no fields")
    }
}

private fun writeField(field: Field, dataOutput: DataOutput, constantPool: ConstantPool) {
    logger.trace("writing Field {} {}", field.name, field.descriptor)
    dataOutput.writeShort(field.accessBitmap)
    dataOutput.writeShort(constantPool.getIndexOfUtfString(field.name))
    dataOutput.writeShort(constantPool.getIndexOfUtfString(field.descriptor))
    writeAttributes(field.attributes, dataOutput, constantPool)
}

private fun writeMethods(methods: List<Method>, dataOutput: DataOutput, constantPool: ConstantPool) {
    logger.trace("writing methods")
    val methodCount: Int = methods.size
    dataOutput.writeShort(methodCount)
    if (methodCount > 0) {
        logger.trace("method count : {}", methodCount)
        for (method in methods) {
            writeMethod(method, dataOutput, constantPool)
        }
    } else {
        logger.trace("no methods")
    }
}

private fun writeMethod(method: Method, dataOutput: DataOutput, constantPool: ConstantPool) {
    logger.trace("writing method {} {}", method.name, method.descriptor)
    dataOutput.writeShort(method.accessBitmap)
    dataOutput.writeShort(constantPool.getIndexOfUtfString(method.name))
    dataOutput.writeShort(constantPool.getIndexOfUtfString(method.descriptor))
    writeAttributes(method.attributes, dataOutput, constantPool)
}




