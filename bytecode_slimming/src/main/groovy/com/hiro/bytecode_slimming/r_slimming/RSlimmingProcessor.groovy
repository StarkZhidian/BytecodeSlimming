package com.hiro.bytecode_slimming.r_slimming

import com.hiro.bytecode_slimming.constants_field_rm.BaseConstantFieldSlimmingProcessor
import com.hiro.bytecode_slimming.Utils

/**
 * R 文件瘦身处理器
 */
class RSlimmingProcessor extends BaseConstantFieldSlimmingProcessor {
    private static final String TAG = "RSlimmingProcessor"

    private static final String[] R_CLASS_PURE_NAME = ['R$anim',
                                                       'R$array',
                                                       'R$attr',
                                                       'R$bool',
                                                       'R$color',
                                                       'R$dimen',
                                                       'R$drawable',
                                                       'R$id',
                                                       'R$integer',
                                                       'R$layout',
                                                       'R$mipmap',
                                                       'R$raw',
                                                       'R$string',
                                                       'R$style',
                                                       'R$styleable',
                                                       'R$xml']

    private RSlimmingProcessor() {
        super()
    }

    static RSlimmingProcessor getInstance() {
        return InstanceHolder.INSTANCE
    }

    private static boolean isRClass(String className) {
        if (Utils.isEmpty(className)) {
            return false
        }
        String pureClassName = Utils.getPureClassName(className)
        for (String rClassName : R_CLASS_PURE_NAME) {
            if (Utils.textEquals(pureClassName, rClassName)) {
                return true
            }
        }
        return false
    }

    @Override
    protected boolean canRemoveConstantFields(String className) {
        return isRClass(className)
    }

    @Override
    protected String getLogTag() {
        return TAG
    }

    private static class InstanceHolder {
        static final RSlimmingProcessor INSTANCE = new RSlimmingProcessor()
    }
}