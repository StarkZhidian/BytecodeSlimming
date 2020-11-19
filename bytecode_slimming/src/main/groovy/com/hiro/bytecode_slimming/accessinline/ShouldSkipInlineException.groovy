package com.hiro.bytecode_slimming.accessinline

/**
 * 该异常类型表示在验证 access$ 方法内部的字节码时发现某些 access$ 方法不能被内联
 */
class ShouldSkipInlineException extends RuntimeException {

    ShouldSkipInlineException(String s) {
        super(s)
    }
}