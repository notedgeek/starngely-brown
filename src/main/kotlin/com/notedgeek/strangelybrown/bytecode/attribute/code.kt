package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput
import java.util.*
import kotlin.collections.HashMap

internal class CodeAttribute : Attribute() {
    var maxStack = 0
    var maxLocals = 0
    val exceptionTable: MutableList<ExceptionTableEntry> = ArrayList()
    var attributes: Map<String, Attribute> = HashMap()
    var code: ByteArray = byteArrayOf()

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        maxStack = dataInput.readUnsignedShort()
        maxLocals = dataInput.readUnsignedShort()
        val codeLength = dataInput.readInt()
        logger.trace("maxStack: {}, maxLocals: {}, codeLength: {}", maxStack, maxLocals, codeLength)
        code = ByteArray(codeLength)
        dataInput.readFully(code)
        logger.trace("code segment")
        for (b in code) {
            logger.trace(String.format("%02X", b))
        }
        val exceptionTableLength = dataInput.readUnsignedShort()
        logger.trace("exceptionTableLength: {}", exceptionTableLength)
        for (i in 0 until exceptionTableLength) {
            val entry = ExceptionTableEntry.fromDataInput(dataInput)
            exceptionTable.add(entry)
            logger.trace(
                "{} startPc : {}, endPc : {}, handlerPc : {}, catchType : {}",
                i, entry.startPc, entry.endPc, entry.handlerPc, entry.catchType
            )
        }
        attributes = loadAttributes(dataInput, constantPool)
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        dataOutput.writeShort(maxStack)
        dataOutput.writeShort(maxLocals)
        dataOutput.writeInt(code.size)
        dataOutput.write(code)
        val exceptionTableLength: Int = exceptionTable.size
        dataOutput.writeShort(exceptionTableLength)
        logger.trace("exceptionTableLength: {}", exceptionTable.size)
        for (i in 0 until exceptionTableLength) {
            val entry = exceptionTable[i]
            entry.toDataOutput(dataOutput)
            logger.trace("{} startPc : {}, endPc : {}, handlerPc : {}, catchType : {}",
                i, entry.startPc, entry.endPc, entry.handlerPc, entry.catchType
            )
        }
        writeAttributes(attributes, dataOutput, constantPool)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CodeAttribute::class.java)
    }

    init {
        name = "Code"
    }
}


internal class ExceptionTableEntry private constructor(
    var startPc: Int,
    var endPc: Int,
    var handlerPc: Int,
    var catchType: Int
) {
    fun toDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(startPc)
        dataOutput.writeShort(endPc)
        dataOutput.writeShort(handlerPc)
        dataOutput.writeShort(catchType)
    }

    companion object {
        fun fromDataInput(dataInput: DataInput): ExceptionTableEntry {
            return ExceptionTableEntry(
                dataInput.readUnsignedShort(), dataInput.readUnsignedShort(),
                dataInput.readUnsignedShort(), dataInput.readUnsignedShort()
            )
        }
    }
}


