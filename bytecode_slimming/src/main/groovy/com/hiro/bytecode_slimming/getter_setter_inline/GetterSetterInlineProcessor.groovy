package com.hiro.bytecode_slimming.getter_setter_inline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * getter/setter 方法内联处理器
 *
 * @author hongweiqiu
 */
class GetterSetterInlineProcessor extends BaseMethodInlineProcessor {

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private GetterSetterInlineProcessor() {
    }

    @Override
    void onAcceptClassFiles(List<File> classList) {
        // first traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            cr.accept(new GetterSetterInlineFirstClassVisitor(Opcodes.ASM6, null), ClassReader.EXPAND_FRAMES)
        }
        // third traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new GetterSetterInlineSecondClassVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            // write final bytes data to origin file
            Utils.write2File(classWriter.toByteArray(), new File(classFile.parentFile, classFile.name))
        }
    }

    static class InstanceHolder {
        static final def INSTANCE = new GetterSetterInlineProcessor();
    }
}