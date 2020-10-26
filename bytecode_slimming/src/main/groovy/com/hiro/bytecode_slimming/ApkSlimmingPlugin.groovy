package com.hiro.bytecode_slimming

import com.android.build.gradle.AppExtension
import com.hiro.bytecode_slimming.accessinline.AccessMethodInlineProcessor
import com.hiro.bytecode_slimming.rm_not_runtime_annotation.AnnotationRemoveProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project

class ApkSlimmingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def androidExtension = project.extensions.getByType(AppExtension)
        def apkSlimmingTransform = new ApkSlimmingTransform(project)
        // 添加 access$xxx 方法内联 processor
        apkSlimmingTransform.addProcessor(AccessMethodInlineProcessor.getInstance())
        // 添加运行时不可见 annotation 注解去除 processor
        apkSlimmingTransform.addProcessor(AnnotationRemoveProcessor.getInstance())
        // 注册 transform
        androidExtension.registerTransform(apkSlimmingTransform)
    }
}