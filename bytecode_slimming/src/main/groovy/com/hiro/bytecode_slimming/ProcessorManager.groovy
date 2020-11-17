package com.hiro.bytecode_slimming

import com.hiro.bytecode_slimming.accessinline.AccessMethodInlineProcessor
import com.hiro.bytecode_slimming.getter_setter_inline.GetterSetterInlineProcessor
import com.hiro.bytecode_slimming.rm_not_runtime_annotation.AnnotationRemoveProcessor

/**
 * 所有的 class Processor 管理器
 * @author hongweiqiu
 */
class ProcessorManager {
    static final def KEY_ACCESS_METHOD_INLINE = "access_method_inline_processor"
    static final def KEY_NOT_RUNTIME_ANNOTATION_REMOVE = "not_runtime_annotation_remove_processor"
    static final def KEY_GETTER_SETTER_METHOD_INLINE = "getter_setter_method_inline_processor"
    static final def KEY_LINE_NUMBER_SAVE = "line_number_save_processor"

    private Map<String, BaseProcessor> processorMap = new HashMap<>()

    private ProcessorManager() {
        processorMap.put(KEY_ACCESS_METHOD_INLINE, AccessMethodInlineProcessor.getInstance())
        processorMap.put(KEY_NOT_RUNTIME_ANNOTATION_REMOVE, AnnotationRemoveProcessor.getInstance())
        processorMap.put(KEY_GETTER_SETTER_METHOD_INLINE, GetterSetterInlineProcessor.getInstance())
    }

    static def getInstance() {
        return InstanceHolder.INSTANCE
    }

    /**
     * 根据指定的 processKey 获取对应的 process 对象
     * @param processorKey 指定的 processKey
     * @return 对应的 processor 对象，可能为 null
     */
    BaseProcessor getProcessor(String processorKey) {
        if (Utils.isEmpty(processorKey)) {
            return null
        }
        return processorMap.get(processorKey)
    }

    /**
     * 获取所有注册的 processor 集合
     */
    Set<BaseProcessor> processors() {
        return processorMap.values()
    }

    private static class InstanceHolder {
        static final def INSTANCE = new ProcessorManager()
    }

}