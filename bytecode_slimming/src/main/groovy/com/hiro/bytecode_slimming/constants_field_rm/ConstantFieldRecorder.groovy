package com.hiro.bytecode_slimming.constants_field_rm

import com.hiro.bytecode_slimming.Utils

/**
 * 要移除类文件中的常量字段的值记录器
 */
class ConstantFieldRecorder {

    private Map<String, Object> rFieldRecorderMap = new HashMap<>()

    private ConstantFieldRecorder() {
    }

    static ConstantFieldRecorder getInstance() {
        return InstanceHolder.INSTANCE
    }

    void appendRField(String className, String fieldName, Object fieldValue) {
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
        static final ConstantFieldRecorder INSTANCE = new ConstantFieldRecorder()
    }

}