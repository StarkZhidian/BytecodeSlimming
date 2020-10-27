package com.hiro.bytecode_slimming

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * Transform 类
 */
class ApkSlimmingTransform extends Transform {
    private static final def TAG = "ApkSlimmingTransform"

    Project project
    /* class/jar 处理器列表 */
    List<BaseProcessor> processorList = new ArrayList<>()

    ApkSlimmingTransform(Project project) {
        this.project = project
    }

    void addProcessor(BaseProcessor processor) {
        processorList.add(processor)
    }

    @Override
    String getName() {
        return TAG;
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        def jarFileList = getJarFileList(inputs)
        def classFileList = getClassFileList(inputs)
        processorList.each { processor ->
            processor.optimizeStart()
            processor.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
            processor.acceptJarFiles(jarFileList)
            processor.acceptClassFiles(classFileList)
            processor.optimizeEnd()
        }
        // 处理完成之后进行 jar/class 目录的拷贝
        copyJars(inputs, outputProvider)
        copyClassDir(inputs, outputProvider)
    }

    /**
     * 获取输入中的 jar 文件列表
     * @param inputs 输入信息
     * @return
     */
    def getJarFileList(Collection<TransformInput> inputs) {
        def jarFileList = new ArrayList()
        inputs.each { input ->
            // 对类型为jar 文件的 input 进行遍历
            input.jarInputs.each { jarInput ->
                //jar文件一般是第三方依赖库jar文件，添加到 list 中
                if (jarInput.file != null && jarInput.file.isFile() && jarInput.name.endsWith(".jar")) {
                    jarFileList.add(jarInput.file)
                }
            }
        }
        return jarFileList
    }

    /**
     * 获取 class 文件列表
     * @param inputs 输入信息
     * @return
     */
    def getClassFileList(Collection<TransformInput> inputs) {
        def classFileList = new ArrayList()
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件
            input.directoryInputs.each { directoryInput ->
                directoryInput.file.eachFileRecurse { file ->
                    if (file.isFile()) {
                        // 添加到 list
                        classFileList.add(file)
                    }
                }
            }
        }
        return classFileList
    }

    /**
     * 拷贝 jar 文件到目标输出目录的方法
     * @param inputs 输入信息
     * @param outputProvider 目标输出目录信息
     */
    void copyJars(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        inputs.each { input ->
            // 对类型为jar 文件的 input 进行遍历
            input.jarInputs.each { jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    /**
     * 拷贝 class 文件所在文件夹到目标输出目录的方法
     * @param inputs inputs 输入信息
     * @param outputProvider 目标输出目录信息
     */
    void copyClassDir(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件的目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest);
            }
        }
    }
}
