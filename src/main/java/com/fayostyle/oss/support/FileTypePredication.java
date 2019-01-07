package com.fayostyle.oss.support;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author keith.huang
 * @date 2019/1/5 14:34
 */
public class FileTypePredication {

    public final static String DEFAULT_MIME = "application/octet-stream";

    public final static int BYTE_NUM_TO_GUESS = 4;

    private static Map<String, String> hex2FileTypeMapping;

    private static Map<String, String> mimeMapping;

    static {
        try(InputStream mimeInput = FileTypePredication.class.getResourceAsStream("/mime.mapping");
            InputStream typeInput = FileTypePredication.class.getResourceAsStream("/hex.filetype")) {

            mimeMapping = loadResource(mimeInput);
            hex2FileTypeMapping = loadResource(typeInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取mime-type
     * @param fileBin  文件二进制数组
     * @param literalExtension 从uploadPath提取出来的后缀名
     * @return
     */
    public static String acquireMimeByBytes(byte[] fileBin, String literalExtension) {

        if(fileBin.length < 4) {
            //字节数组小于4，无法判断，返回默认二进制
            return DEFAULT_MIME;
        }

        String fileHex = bin2Hex(fileBin, 0, BYTE_NUM_TO_GUESS);
        String extension = hex2FileTypeMapping.get(fileHex);

        if(extension.contains("&") && !Strings.isNullOrEmpty(literalExtension)
                && extension.contains(literalExtension)) {
            /**针对以下情况
             * FFD8FFE0  jpg&jpeg
             * FFD8FFE1  jpg&jpeg
             * D0CF11E0  xls&doc
             * 504b0304  xls&docx
             */
            return mimeMapping.get(literalExtension);
        } else if(extension.contains("&")) {
            return DEFAULT_MIME;
        }

        return mimeMapping.get(extension);

    }


    //二进制转十六进制
    private static String bin2Hex(byte[] file, int offSet, int length) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<length; i++) {
            //按位与，得到十进制数据大小。
            int t = file[i] & 0xFF;

            if(t < 16) {
                //Integer.toHexString(15) 返回 F
                //Integer.toHexString(16) 返回 10
                //故做添0处理
                sb.append("0" + Integer.toHexString(t));
            } else {
                sb.append(Integer.toHexString(t));
            }
        }

        return sb.toString();
    }


     public static Map<String, String> loadResource(InputStream input) throws IOException {
        Map<String, String> resourceMap = new HashMap<>(10);
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line = null;

        while((line = br.readLine()) != null) {
            line = line.trim();

            if(line.startsWith("#") || line.length() == 0) {

            } else {
                String[] args = line.split(" ", 2);
                resourceMap.put(args[0].trim(), args[1].trim());
            }
        }
        return resourceMap;
    }

}
