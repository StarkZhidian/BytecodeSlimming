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
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

/**
 * Transform 类
 */
class ApkSlimmingTransform extends Transform {
    private static final def TAG = "ApkSlimmingTransform"

    final Project project
    /* class/jar 处理器列表 */
    private List<BaseProcessor> processorList = new ArrayList<>()
    /* jar 文件中过滤 class 文件的处理器 */
    private Utils.UncompressFileFilter classFileUncompressFilter = new ClassFileUncompressFilter()
    /* Jar 文件解压监听处理器 */
    private Utils.JarUncompressListener jarUncompressListener = new JarFileUncompressListener()

    ApkSlimmingTransform(Project project) {
        this.project = project
    }

    void addProcessor(BaseProcessor processor) {
        processorList.add(processor)
    }

    @Override
    String getName() {
        return TAG
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        long allStartTime = System.currentTimeMillis()
        handleJarFileList(inputs)
        handleClassFileList(inputs)
        Logger.d3(TAG, "transform, class 信息读取完成, 读取的 class 数：${ClassDataManager.getClassDataSize()}")
        processorList.each { processor ->
            try {
                long startTime = System.currentTimeMillis()
                processor.optimizeStart()
                // 这里为什么要每次重新获取一次 ClassDataList，
                // 因为每个处理器在执行过程中可能会修改现有的类信息对象集合，
                // 所以每次都要重新获取保证每次都是获取到最新的类信息
                processor.accept(ClassDataManager.getClassDataList())
                processor.optimizeEnd()
                long endTime = System.currentTimeMillis()
                Logger.d3(TAG, "处理器 $processor 执行耗时：[${Utils.millSecond2Second(endTime - startTime)}] 秒")
            } catch (Throwable t) {
                Logger.e(TAG, "unexpected exception was occurred in processor = [$processor],"
                        + "\nexceptions: ", t)
            }
        }
        // 处理完成之后进行 class 文件的拷贝
        copyClassDir(inputs, outputProvider)
        Logger.d3(TAG, "BytecodeSlimming 插件运行完成，" +
                "耗时 [${Utils.millSecond2Second(System.currentTimeMillis() - allStartTime)}] 秒")
    }

    /**
     * 获取输入中的 jar 文件列表
     * @param inputs 输入信息
     * @return
     */
    private void handleJarFileList(Collection<TransformInput> inputs) {
        int uncompressJarFileCount = 0
        final File compiledClassesDir = getFirstCompiledClassesDir(inputs)
        inputs.each { input ->
            // 对类型为jar 文件的 input 进行遍历
            input.jarInputs.each { jarInput ->
                //jar文件一般是第三方依赖库jar文件，添加到 list 中
                File file = jarInput.getFile()
                if (Utils.isValidFile(file) && Utils.isJarFile(file)) {
                    // 生成输出路径，将 jar 里面的 class 文件看成用户自己输入的 class 代码，
                    // 解压到对应目录下，并对解压后的 .class 文件生成对应的 ClassModel 对象
                    Utils.uncompressJarFile(file, compiledClassesDir,
                            classFileUncompressFilter, jarUncompressListener)
                    // 解压的 jar 文件数目加 1
                    uncompressJarFileCount++
                }
            }
        }
        Logger.d3(TAG, "handleJarFileList, jar 文件解压完成，共解压：$uncompressJarFileCount 个文件")
    }

    /**
     * 获取 class 文件列表
     * @param inputs 输入信息
     * @return
     */
    private void handleClassFileList(Collection<TransformInput> inputs) {
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件
            input.directoryInputs.each { directoryInput ->
                directoryInput.file.eachFileRecurse { file ->
                    if (Utils.isClassFile(file)) {
                        // class 文件，直接解析生成对应的 ClassModel 对象
                        generateSingleClassModel(file, null)
                    }
                }
            }
        }
    }

    /**
     * 生成 .class 文件对应的 ClassModel 对象
     *
     * @param classFile .class 文件对象
     * @param jarFilePath 当前 .class 文件是从哪个 jar 文件中解压出来的，
     *                    如果是项目内创建的源码文件编译得到的 .class 文件，则传 null
     * @return 是否
     */
    private boolean generateSingleClassModel(File classFile, String jarFilePath) {
        Logger.d1(TAG, "generateSingleClassModel, classFile = [${classFile.getAbsolutePath()}," +
                " jarFilePath = [$jarFilePath]")
        if ((!Utils.isValidFile(classFile)) || (!Utils.isClassFile(classFile))) {
            return false
        }
        try {
            ClassNode classParser = new ClassNode()
            new ClassReader(classFile.bytes).accept(classParser, ClassReader.EXPAND_FRAMES)
            ClassDataManager.addClassModel(new SingleClassData(
                    classParser.name, classFile, classParser.superName, jarFilePath))
            return true
        } catch (Exception exception) {
            Logger.e(TAG, "generateSingleClassModel", exception)
        }
        return false
    }

    private static File getFirstCompiledClassesDir(Collection<TransformInput> inputs) {
        File resultDir = null
        inputs.each { input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (resultDir == null || (!resultDir.isDirectory())) {
                    Logger.d3(TAG, "getFirstCompiledClassesDir = " + directoryInput.file.getAbsolutePath())
                    // 获取 java 源代码编译成 class 的目录，
                    // 一般为 ${projectPath}\${module}\build\intermediates\javac\${buildType}\classes 文件夹
                    resultDir = directoryInput.file
                }
            }
        }
        return resultDir
    }

    /**
     * 这里需要将编译后最终得到的 .class 文件输送到下一个 transform 的输入位置
     * @param inputs
     * @param outputProvider
     */
    private static void copyClassDir(Collection<TransformInput> inputs,
                                     TransformOutputProvider outputProvider) {
        long startTime = System.currentTimeMillis()
        inputs.each { input ->
            // 这里一般是自己编写的 .class 文件的目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                Logger.d3(TAG, "copyClassDir, from dir = [" + directoryInput.file.getAbsolutePath() + "], to dir = [" + dest.getAbsolutePath() + "]")
                FileUtils.copyDirectory(directoryInput.file, dest);
            }
        }
        long endTime = System.currentTimeMillis()
        Logger.d3(TAG, "复制类文件到下一个 transform 的类输入文件夹目录，" +
                "耗时 [${Utils.millSecond2Second(endTime - startTime)}] 秒")
    }

    /**
     * 项目中引入的 sdk 中 jar 内部文件的解压过滤器
     */
    private class ClassFileUncompressFilter implements Utils.UncompressFileFilter {

        @Override
        boolean canUncompress(String uncompressedFilePath, String fromJarFilePath) {
            // 只解压 .class 文件
            return ((!Utils.isEmpty(uncompressedFilePath))
                    && (uncompressedFilePath.endsWith(Constants.CLASS_FILE_SUFFIX)))
        }
    }

    /**
     * 项目中引入的 sdk 中 jar 文件的解压监听
     */
    private class JarFileUncompressListener implements Utils.JarUncompressListener {

        @Override
        void onUncompressSingleFile(File uncompressedFile, String fromJarFilePath) {
            if (Utils.isValidFile(uncompressedFile) && Utils.isClassFile(uncompressedFile)) {
                // 当前文件如果是合法的 .class 文件，则生成对应的 ClassModel 对象
                generateSingleClassModel(uncompressedFile, fromJarFilePath)
            }
        }
    }
}
