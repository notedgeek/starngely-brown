package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

internal class ConstantValueAttribute : Attribute() {
    private var constantValueIndex = 0

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        constantValueIndex = dataInput.readUnsignedShort()
        logger.debug("index : $constantValueIndex")
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        dataOutput.writeShort(constantValueIndex)
        logger.debug("index : $constantValueIndex")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Attribute::class.java)
    }

    init {
        name = "ConstantValue"
    }
}
