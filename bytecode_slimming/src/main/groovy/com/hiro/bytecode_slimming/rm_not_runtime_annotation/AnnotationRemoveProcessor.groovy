package com.hiro.bytecode_slimming.rm_not_runtime_annotation

import com.hiro.bytecode_slimming.BaseProcessor
import com.hiro.bytecode_slimming.ClassDataManager
import com.hiro.bytecode_slimming.Logger
import com.hiro.bytecode_slimming.SingleClassData
import com.hiro.bytecode_slimming.Utils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import com.hiro.bytecode_slimming.Constants

/**
 * Class 文件中运行时不可见的注解清除处理器
 * @author hongweiqiu
 */
class AnnotationRemoveProcessor extends BaseProcessor {

    /* 保存被移除的非运行时注解类型 */
    private List<String> removedAnnotationList = new LinkedList<>()
    /* 记录被删除的运行时不可见的注解类文件数目 */
    private int deletedAnnotationClassFileCount

    private AnnotationRemoveProcessor() {
    }

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    void addRemovedAnnotation(String annotationClassDesc) {
        if (Utils.isEmpty(annotationClassDesc)
                || removedAnnotationList.contains(annotationClassDesc)) {
            return
        }
        removedAnnotationList.add(annotationClassDesc)
    }

    /**
     * 判断某个不可见注解能否被移除（是否在 keepClassNameList 内）
     */
    boolean annotationCanRemove(String annotationClassDesc) {
        if (Utils.isEmpty(annotationClassDesc)) {
            return false
        }
        return !inKeepClassList(getAnnotationClassName(annotationClassDesc))
    }

    private String getAnnotationClassName(String annotationClassDesc) {
        if (Utils.isEmpty(annotationClassDesc)) {
            removedAnnotationList annotationClassDesc
        }
        int startPos = 0
        int endPos = annotationClassDesc.length()
        if (annotationClassDesc.startsWith(Constants.CUSTOM_CLASS_DESC_PREFIX)) {
            startPos++
        }
        if (annotationClassDesc.endsWith(Constants.CLASS_DESC_SUFFIX)) {
            endPos--
        }
        return annotationClassDesc.substring(startPos, endPos)
    }

    @Override
    void onAccept(List<SingleClassData> classDataList) {
        // 将符合条件的非运行时注解的使用处移除
        classDataList.each { classModel ->
            ClassReader cr = new ClassReader(classModel.fileBytes)
            ClassWriter classWriter = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            cr.accept(new ClassAnnotationRmVisitor(Constants.ASM_VERSION, classWriter), ClassReader.EXPAND_FRAMES)
            Utils.write2File(classWriter.toByteArray(), classModel.classFile)
        }
        // 将不再使用的非运行时注解对应的类信息和类文件删除
        removedAnnotationList.each { annotationClassDesc ->
            deletedAnnotationClassFileCount += (annotationCanRemove(annotationClassDesc)
                    && ClassDataManager.removeClassAndDeleteFile(getAnnotationClassName(annotationClassDesc))) ? 1 : 0
        }
    }

    @Override
    void onOptimizeEnd() {
        Logger.d3(TAG, "处理器：[$this] 删除了: [$deletedAnnotationClassFileCount] 个注解类文件")
        // recycle memory
        removedAnnotationList.clear()
        deletedAnnotationClassFileCount = 0
        super.onOptimizeEnd()
    }

    private static class InstanceHolder {
        static final def INSTANCE = new AnnotationRemoveProcessor()
    }
}