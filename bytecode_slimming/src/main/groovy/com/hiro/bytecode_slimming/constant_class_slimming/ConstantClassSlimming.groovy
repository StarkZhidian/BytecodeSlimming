package com.hiro.bytecode_slimming.constant_class_slimming

import com.hiro.bytecode_slimming.Utils
import com.hiro.bytecode_slimming.constants_field_rm.BaseConstantFieldSlimmingProcessor

/**
 * 业务类中常量字段移除处理器
 */
class ConstantClassSlimming extends BaseConstantFieldSlimmingProcessor {
    private static final String TAG = "ConstantClassSlimming"

    /* 记录需要进行常量字段移除的类名列表（每个元素为 JVM 层类描述，例：java/lang/String） */
    private final List<String> slimmingClassList = new LinkedList<>()

    /**
     * 添加要进行常量值字段移除的类名
     */
    void appendClass(List<String> classNameList) {
        if (classNameList == null || classNameList.isEmpty()) {
            return
        }
        for (String className : classNameList) {
            if (!slimmingClassList.contains(className)) {
                slimmingClassList.add(className)
            }
        }
    }

    void removeClass(List<String> classNames) {
        if (classNames == null || classNames.isEmpty()) {
            return
        }
        slimmingClassList.removeAll(classNames)
    }

    static ConstantClassSlimming getInstance() {
        return InstanceHolder.INSTANCE
    }

    private ConstantClassSlimming() {
        super()
    }

    @Override
    protected boolean canRemoveConstantFields(String className) {
        if (Utils.isEmpty(className)) {
            return false
        }
        for (String listClassName : slimmingClassList) {
            if (Utils.textEquals(className, listClassName)) {
                return true;
            }
        }
        return false
    }

    @Override
    protected String getLogTag() {
        return TAG
    }

    private static class InstanceHolder {
        static final ConstantClassSlimming INSTANCE = new ConstantClassSlimming()
    }
}