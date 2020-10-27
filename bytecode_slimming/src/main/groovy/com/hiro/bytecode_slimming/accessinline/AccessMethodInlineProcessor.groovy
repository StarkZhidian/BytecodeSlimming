package com.hiro.bytecode_slimming.accessinline

import com.hiro.bytecode_slimming.BaseMethodInlineProcessor
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * access$xxx 方法内联处理器
 * @author hongweiqiu
 */
class AccessMethodInlineProcessor extends BaseMethodInlineProcessor {

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    private AccessMethodInlineProcessor() {
    }

    def getAccessMethodInfo(def className, def methodName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(methodName)) {
            return null
        }
        return methodInlineInfoMap.get(makeInlineMethodInfoKey(className, methodName))
    }

    @Override
    void onAcceptClassFiles(List<File> classList) {
        // first traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            cr.accept(new AccessMethodInlineFirstClassVisitor(Opcodes.ASM6, null), ClassReader.EXPAND_FRAMES)
        }
        // second traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            cr.accept(new AccessMethodInlineSecondClassVisitor(Opcodes.ASM6, null), ClassReader.EXPAND_FRAMES)
        }
        // third traversal
        classList.each { classFile ->
            ClassReader cr = new ClassReader(classFile.bytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new AccessMethodInlineThirdClassVisitor(Opcodes.ASM6, classWriter), ClassReader.EXPAND_FRAMES)
            // write final bytes data to origin file
            Utils.write2File(classWriter.toByteArray(), new File(classFile.parentFile, classFile.name))
        }
    }

    static class InstanceHolder {
        static final def INSTANCE = new AccessMethodInlineProcessor();
    }
}