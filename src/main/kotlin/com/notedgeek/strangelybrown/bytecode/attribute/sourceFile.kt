package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

internal class SourceFileAttribute : Attribute() {
    lateinit var sourceFileName: String

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        sourceFileName = constantPool.getUtfValue(dataInput)
        logger.trace("source file : {} ", sourceFileName)
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        dataOutput.writeShort(constantPool.getIndexOfUtfString(sourceFileName))
        logger.trace("source file : {} ", sourceFileName)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SourceFileAttribute::class.java)
    }

    init {
        name = "SourceFile"
    }
}
