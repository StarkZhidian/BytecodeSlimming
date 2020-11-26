package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.Utils
import jdk.internal.org.objectweb.asm.tree.FieldNode

/**
 * 要移除的 R 类文件中的字段记录器
 */
class RFieldRecorder {

    private Map<String, Integer> rFieldRecorderMap = new HashMap<>()

    private RFieldRecorder() {
    }

    static RFieldRecorder getInstance() {
        return InstanceHolder.INSTANCE
    }

    void appendRField(String className, String fieldName, int fieldValue) {
        if (Utils.isEmpty(className) || Utils.isEmpty(fieldName)) {
            return
        }
        rFieldRecorderMap.put(makeRFieldKey(className, fieldName), fieldValue)
    }

    Integer getRFieldValue(String className, String fieldName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(fieldName)) {
            return null
        }
        return rFieldRecorderMap.get(makeRFieldKey(className, fieldName))
    }

    private static String makeRFieldKey(String className, String fieldName) {
        if (Utils.isEmpty(className) || Utils.isEmpty(fieldName)) {
            throw IllegalArgumentException("className or fieldName can not be empty!")
        }
        return "$className#$fieldName"
    }

    private static final class InstanceHolder {
        static final RFieldRecorder INSTANCE = new RFieldRecorder()
    }

}