package com.hiro.bytecode_slimming

import org.objectweb.asm.Opcodes

/**
 * 常量值工具类
 */
class Constants {

    static final String CLASS_FILE_SUFFIX = ".class"

    static final int ASM_VERSION = Opcodes.ASM6

    static final String ACCESS_METHOD_NAME_PREFIX = 'access$'

    static final String VERSION = "1.2.5"

    /* JVM 中非基本类型描述以 L 开头 */
    static final String CLASS_DESC_PREFIX = 'L'

    /* JVM 中非基本类型描述以 ; 结尾 */
    static final String CLASS_DESC_SUFFIX = ';'
}