package com.fayostyle.oss;

/**
 * @author keith.huang
 * @date 2019/1/4 16:52
 */
public class OssException extends RuntimeException {
    public OssException() {
        super();
    }

    public OssException(String msg) {
        super(msg);
    }

    public OssException(String message, Throwable cause) {
        super(message, cause);
    }

    public OssException(Throwable cause) {
        super(cause);
    }

    protected OssException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
