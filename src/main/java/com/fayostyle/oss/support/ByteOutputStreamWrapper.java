package com.fayostyle.oss.support;

import java.io.ByteArrayOutputStream;

/**
 * @author keith.huang
 * @date 2019/1/4 17:31
 */
public class ByteOutputStreamWrapper extends ByteArrayOutputStream {
    public byte[] acquireZeroCopyBuf() {
        return super.buf;
    }
}
