package com.fayostyle.oss.util;

/**
 * @author keith.huang
 * @date 2019/1/7 10:56
 */
public class SignUtils {

    public static String buildLocalCanonicalString(String bizId, String accessType, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(bizId).append("\n")
                .append(accessType).append("\n")
                .append(path);

        return sb.toString();
    }
}
