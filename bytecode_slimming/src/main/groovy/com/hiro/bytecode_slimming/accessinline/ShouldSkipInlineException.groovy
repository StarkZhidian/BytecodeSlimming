package com.hiro.bytecode_slimming.accessinline


class ShouldSkipInlineException extends RuntimeException {


    ShouldSkipInlineException() {
        super()
    }

    ShouldSkipInlineException(String s) {
        super(s)
    }

    ShouldSkipInlineException(String s, Throwable throwable) {
        super(s, throwable)
    }

    ShouldSkipInlineException(Throwable throwable) {
        super(throwable)
    }

    protected ShouldSkipInlineException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1)
    }
}