package com.fayostyle.oss.util;

import com.fayostyle.oss.support.ByteOutputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author keith.huang
 * @date 2019/1/5 12:50
 */
public class FileUtils {
    public static ByteOutputStreamWrapper transfer2MemoryOutputStream(InputStream input) throws IOException {
        ByteOutputStreamWrapper out = new ByteOutputStreamWrapper();
        byte[] bytes = new byte[1024 * 4];
        int len;
        while((len = input.read(bytes)) != -1) {
            out.write(bytes, 0, len);
        }

        return out;
    }
}
