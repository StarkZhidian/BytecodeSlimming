package com.hiro.bytecode_slimming.constants_field_rm

import com.hiro.bytecode_slimming.BaseClassVisitor
import org.objectweb.asm.ClassVisitor

/**
 * 类文件常量字段移除处理器中的类访问器基类
 */
class BaseConstantFieldSlimmingClassVisitor extends BaseClassVisitor {

    /* 当前类中的数据是否被更改过 */
    protected boolean dataIsChanged

    BaseConstantFieldSlimmingClassVisitor(int api) {
        super(api)
    }

    BaseConstantFieldSlimmingClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    boolean getDataIsChanged() {
        return dataIsChanged
    }

}