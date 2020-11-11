package com.notedgeek.strangelybrown.bytecode.attribute

import com.notedgeek.strangelybrown.bytecode.ConstantPool
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput
import java.util.*

internal class LineNumberTableAttribute : Attribute() {

    lateinit var entries: MutableList<LineNumberTableEntry>

    override fun loadFromDataInput(dataInput: DataInput, constantPool: ConstantPool) {
        val tableLength = dataInput.readUnsignedShort()
        logger.trace("length : {}", tableLength)
        entries = ArrayList()
        for (i in 0 until tableLength) {
            val entry = LineNumberTableEntry.fromDataInput(dataInput)
            entries.add(entry)
            logger.trace("{} spc : {} -> ln : {}", i, entry.startPc, entry.lineNumber)
        }
    }

    override fun writeContentsToDataOutput(dataOutput: DataOutput, constantPool: ConstantPool) {
        val tableLength = entries.size
        dataOutput.writeShort(tableLength)
        for (i in 0 until tableLength) {
            val entry = entries[i]
            entry.toDataOutput(dataOutput)
            logger.debug("{} spc : {} -> ln : {}", i, entry.startPc, entry.lineNumber)
        }
    }

    init {
        name = "LineNumberTable"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LineNumberTableAttribute::class.java)
    }
}

internal class LineNumberTableEntry private constructor(val startPc: Int, val lineNumber: Int) {

    fun toDataOutput(dataOutput: DataOutput) {
        dataOutput.writeShort(startPc)
        dataOutput.writeShort(lineNumber)
    }

    companion object {
        fun fromDataInput(dataInput: DataInput): LineNumberTableEntry {
            return LineNumberTableEntry(dataInput.readUnsignedShort(), dataInput.readUnsignedShort())
        }
    }
}


