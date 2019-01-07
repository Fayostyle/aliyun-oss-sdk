package com.fayostyle.oss.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author keith.huang
 * @date 2019/1/4 16:57
 */
public class StringHelper {
    public final static String ACCESS_TYPES = "private public";
    public final static String COLON = ":";
    public final static String DOT = ".";
    public final static String QUERY_STRING_DILIMETER = "?";
    public final static String SLASH = "/";
    public final static String EQUAL = "=";
    public final static String AND = "&";

    public static String getExtensionFromUri(String uri) {
        String logicFileName = logicFileNameFromUri(uri);

        return StringUtils.substringAfterLast(logicFileName, DOT);
    }

    public static String logicFileNameFromUri(String uri) {
        String uriWithoutQueryString = uriWithoutQueryString(uri);

        return StringUtils.substringAfterLast(uriWithoutQueryString, SLASH);
    }

    public static String uriWithoutQueryString(String uri) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(uri), "uri 不能为空");

        return StringUtils.substringBefore(uri, QUERY_STRING_DILIMETER);
    }

    public static String urlDecode(String encodeUrl) {
        try {
            String str = URLDecoder.decode(encodeUrl, "UTF-8");
            return  str;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodeUrl;
        }
    }
}
