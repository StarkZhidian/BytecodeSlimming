package com.hiro.bytecode_slimming

import org.objectweb.asm.Opcodes

/**
 * 常量值工具类
 */
class Constants {

    static final String CLASS_FILE_SUFFIX = ".class"

    static final int ASM_VERSION = Opcodes.ASM6

    static final String ACCESS_METHOD_NAME_PREFIX = 'access$'

    static final String VERSION = "1.2.7"

    /* JVM (.class 文件中) 中非基本类型描述以 L 开头 */
    static final String CUSTOM_CLASS_DESC_PREFIX = 'L'

    /* JVM (.class 文件中) 中数组类型描述以 [ 开头 */
    static final String ARRAY_CLASS_DESC_PREFIX = '['

    /* JVM (.class 文件中) 中非基本类型描述以 ; 结尾 */
    static final String CLASS_DESC_SUFFIX = ';'

    /* JVM (.class 文件中) 中类名中以 / 作为基本分隔符，例：java/lang/String */
    static final String JVM_CLASS_INTERVAL = '/'
}