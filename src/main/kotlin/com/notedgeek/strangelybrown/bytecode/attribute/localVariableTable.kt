package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

internal class LocalVariableTableAttribute : Attribute() {
    lateinit var entries: MutableList<LocalVariableTableEntry>

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        val tableLength = dataInput.readUnsignedShort()
        logger.trace("length : {}", tableLength)
        entries = ArrayList()
        for (i in 0 until tableLength) {
            val entry = LocalVariableTableEntry.fromDataInput(dataInput, constantPool)
            entries.add(entry)
            logger.trace(
                "{} startPc: {}, length: {}, name: {}, descriptor: {}, index: {}",
                i, entry.startPc, entry.length, entry.name, entry.descriptor, entry.index
            )
        }
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        val tableLength = entries.size
        dataOutput.writeShort(tableLength)
        for (i in 0 until tableLength) {
            val entry = entries[i]
            entry.toDataOutput(dataOutput, constantPool)
            logger.trace(
                "{i} startPc: {}, length: {}, name: {}, descriptor: {}, index: {}",
                i, entry.startPc, entry.length, entry.name, entry.descriptor, entry.index
            )
        }
    }

    internal class LocalVariableTableEntry private constructor() {
        var startPc = 0
        var length = 0
        lateinit var name: String
        lateinit var descriptor: String
        var index = 0
        fun toDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
            dataOutput.writeShort(startPc)
            dataOutput.writeShort(length)
            dataOutput.writeShort(constantPool.getIndexOfUtfString(name))
            dataOutput.writeShort(constantPool.getIndexOfUtfString(descriptor))
            dataOutput.writeShort(index)
        }

        companion object {
            fun fromDataInput(dataInput: DataInput, constantPool: ConstantPool): LocalVariableTableEntry {
                val entry = LocalVariableTableEntry()
                entry.startPc = dataInput.readUnsignedShort()
                entry.length = dataInput.readUnsignedShort()
                entry.name = constantPool.getUtfValue(dataInput)
                entry.descriptor = constantPool.getUtfValue(dataInput)
                entry.index = dataInput.readUnsignedShort()
                return entry
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LocalVariableTableAttribute::class.java)
    }

    init {
        name = "LocalVariableTable"
    }
}

