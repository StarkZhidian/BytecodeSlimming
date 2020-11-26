package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.BaseClassVisitor
import org.objectweb.asm.ClassVisitor

/**
 * R 文件瘦身处理器中的类访问器基类
 */
class BaseRSlimmingClassVisitor extends BaseClassVisitor {

    /* 当前类中的数据是否被更改过 */
    protected boolean dataIsChanged

    BaseRSlimmingClassVisitor(int api) {
        super(api)
    }

    BaseRSlimmingClassVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    boolean getDataIsChanged() {
        return dataIsChanged
    }

}