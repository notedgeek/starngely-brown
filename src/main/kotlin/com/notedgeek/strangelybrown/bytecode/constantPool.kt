package com.notedgeek.strangelybrown.bytecode

import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

internal class ConstantPool(private val size: Int = 10) {

    companion object {
        fun loadAndLink(dataInput: DataInput): ConstantPool = ConstantPool(dataInput.readUnsignedShort()).apply {
            load(dataInput)
            link()
        }

        private val logger = LoggerFactory.getLogger(ConstantPool::class.java)
    }

    private val entries = ArrayList<ConstantPoolEntry?>(size)

    init {
        entries.add(null)
    }

    private fun load(dataInput: DataInput) {
        logger.trace("loading constant pool")
        var entryNumber = 1
        while (entryNumber < size) {
            logger.trace("entry number $entryNumber")
            val tag = dataInput.readByte().toInt()
            val entry = createEntry(tag)
                ?: throw RuntimeException(String.format("unknown tag - %d at entry %d", tag, entryNumber))
            entry.tag = tag
            entry.loadFromDataInput(dataInput)
            entries.add(entry)
            if (skipSlotAfterEntry(entry)) {
                entries.add(null)
                entryNumber++
            }
            entryNumber++
        }
    }

    private fun createEntry(tag: Int): ConstantPoolEntry? {
        return when (tag) {
            METHOD_REF -> ConstantMethodRef()
            CLASS -> ConstantClass()
            STRING -> ConstantString()
            FIELD_REF -> ConstantFieldRef()
            UTF8 -> ConstantUtf8()
            NAME_AND_TYPE -> ConstantNameAndType()
            INTEGER -> ConstantInteger()
            FLOAT -> ConstantFloat()
            LONG -> ConstantLong()
            DOUBLE -> ConstantDouble()
            else -> null
        }
    }

    private fun link() {
        logger.trace("linking constant pool")
        var entryNumber = 1
        while (entryNumber < entries.size) {
            logger.trace("entry number $entryNumber")
            val entry = entries[entryNumber]
            if (entry != null) {
                entry.link(this)
                if (skipSlotAfterEntry(entry)) {
                    entryNumber++
                }
            }
            entryNumber++
        }
    }

    fun writeToDataOutput(dataOutput: DataOutput) {
        logger.trace(
            "creating constant pool with size {} ({} slots)",
            entries.size,
            entries.size - 1
        )
        dataOutput.writeShort(entries.size)
        var entryNumber = 1
        while (entryNumber < entries.size) {
            val entry = entries[entryNumber]!!
            dataOutput.writeByte(entry.tag)
            entry.writeToDataOutput(dataOutput)
            if (skipSlotAfterEntry(entry)) {
                entryNumber++
            }
            entryNumber++
        }
    }

    internal fun ensureClass(className: String): Int {
        val index = getIndexOfClassName(className)
        if (index > 0) {
            return index
        }
        val constantClass = ConstantClass()
        constantClass.name = className
        constantClass.nameIndex = ensureConstantUtfString(className)
        entries.add(constantClass)
        return entries.size - 1
    }

    internal fun ensureConstantString(string: String): Int {
        val index = getIndexOfConstantString(string)
        if (index > 0) {
            return index
        }
        val constantString = ConstantString()
        constantString.utf8Value = string
        constantString.utf8Index = ensureConstantUtfString(string)
        entries.add(constantString)
        return entries.size - 1
    }

    internal fun ensureConstantUtfString(string: String): Int {
        val index = getIndexOfUtfString(string)
        if (index > 0) {
            return index
        }
        val constantUtf8 = ConstantUtf8()
        constantUtf8.value = string
        entries.add(constantUtf8)
        return entries.size - 1
    }

    internal fun ensureMethodRef(className: String, name: String, descriptor: String): Int {
        val index = getIndexOfMethodRef(className, name, descriptor)
        if (index > 0) {
            return index
        }
        val methodRef = ConstantMethodRef()
        methodRef.className = className
        methodRef.classIndex = ensureClass(className)
        methodRef.name = name
        methodRef.descriptor = descriptor
        methodRef.nameAndTypeIndex = ensureNameAndType(name, descriptor)
        entries.add(methodRef)
        return entries.size - 1
    }

    internal fun ensureFieldRef(className: String, name: String, descriptor: String): Int {
        val index = getIndexOfFieldRef(className, name, descriptor)
        if (index > 0) {
            return index
        }
        val fieldRef = ConstantFieldRef()
        fieldRef.className = className
        fieldRef.classIndex = ensureClass(className)
        fieldRef.name = name
        fieldRef.descriptor = descriptor
        fieldRef.nameAndTypeIndex = ensureNameAndType(name, descriptor)
        entries.add(fieldRef)
        return entries.size - 1
    }

    internal fun ensureNameAndType(name: String, descriptor: String): Int {
        val index = getIndexOfNameAndType(name, descriptor)
        if (index > 0) {
            return index
        }
        val nameAndType = ConstantNameAndType()
        nameAndType.name = name
        nameAndType.nameIndex = ensureConstantUtfString(name)
        nameAndType.descriptor = descriptor
        nameAndType.descriptorIndex = ensureConstantUtfString(descriptor)
        entries.add(nameAndType)
        return entries.size - 1
    }

    internal fun getIndexOfUtfString(string: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == UTF8 && (entry as ConstantUtf8).value == string) {
                logger.trace("index of utf8 \"$string\" is $entryNumber")
                return entryNumber
            }
        }
        return -1
    }

    internal fun getIndexOfClassName(string: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == CLASS && (entry as ConstantClass).name == string) {
                logger.trace("index of class \"{}\" is {}", string, entryNumber)
                return entryNumber
            }
        }
        return -1
    }

    internal fun getIndexOfConstantString(string: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == STRING && (entry as ConstantString).utf8Value == string) {
                logger.trace("index of class \"{}\" is {}", string, entryNumber)
                return entryNumber
            }
        }
        return -1
    }

    internal fun getIndexOfMethodRef(className: String, name: String, descriptor: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == METHOD_REF) {
                val methodRef = entry as ConstantMethodRef
                if (methodRef.className == className && methodRef.name == name && methodRef.descriptor == descriptor) {
                    logger.trace("index of method \"{}\" \"{}\" \"{}\" is {}", className, name, descriptor, entryNumber)
                    return entryNumber
                }
            }
        }
        return -1
    }

    internal fun getIndexOfFieldRef(className: String, name: String, descriptor: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == FIELD_REF) {
                val fieldRef = entry as ConstantFieldRef
                if (fieldRef.className == className && fieldRef.name == name && fieldRef.descriptor == descriptor) {
                    logger.trace("index of field \"{}\" \"{}\" \"{}\" is {}", className, name, descriptor, entryNumber)
                    return entryNumber
                }
            }
        }
        return -1
    }

    internal fun getIndexOfNameAndType(name: String, descriptor: String): Int {
        for (entryNumber in 1 until entries.size) {
            val entry = entries[entryNumber]
            if (entry != null && entry.tag == NAME_AND_TYPE) {
                val nameAndType = entry as ConstantNameAndType
                if (nameAndType.name == name && nameAndType.descriptor == descriptor) {
                    logger.debug("index of nameAndType \"{}\" \"{}\" is {}", name, descriptor, entryNumber)
                    return entryNumber
                }
            }
        }
        return -1
    }


    internal operator fun get(index: Int): ConstantPoolEntry? {
        return entries[index]
    }

    internal fun getClassName(dataInput: DataInput) = getClassName(dataInput.readUnsignedShort())

    private fun getClassName(index: Int) = (entries[index] as ConstantClass).name

    internal fun getUtfValue(dataInput: DataInput) = getUtfValue(dataInput.readUnsignedShort())

    internal fun getUtfValue(index: Int) = (entries[index] as ConstantUtf8).value

    private fun skipSlotAfterEntry(entry: ConstantPoolEntry): Boolean {
        return entry is ConstantLong || entry is ConstantDouble
    }

}