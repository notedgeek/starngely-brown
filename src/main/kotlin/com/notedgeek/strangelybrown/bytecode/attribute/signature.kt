package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

internal class SignatureAttribute : Attribute() {
    lateinit var signature: String

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        signature = constantPool.getUtfValue(dataInput)
        logger.trace("signature : {} ", signature)
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        dataOutput.writeShort(constantPool.getIndexOfUtfString(signature))
        logger.trace("signature : {} ", signature)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SourceFileAttribute::class.java)
    }

    init {
        name = "Signature"
    }
}
