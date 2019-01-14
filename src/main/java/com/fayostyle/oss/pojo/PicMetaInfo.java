package com.fayostyle.oss.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;

/**
 * @author keith.huang
 * @date 2019/1/14 9:49
 */
@Data
@ToString
public class PicMetaInfo {

    private long fileSize;

    private String format;

    private int height;

    private int width;

   public static class PicMetaInfoParser {
       private final static String ALI_FILESIZE = "FileSize";
       private final static String ALI_FORMAT = "Format";
       private final static String ALI_HEIGHT = "ImageHeight";
       private final static String ALI_WIDTH = "ImageWidth";

       public static PicMetaInfo parser(String metaInfoStr) {
           JSONObject metaJson = JSON.parseObject(metaInfoStr);
           PicMetaInfo picMetaInfo = new PicMetaInfo();

           picMetaInfo.setFileSize(metaJson.getJSONObject(ALI_FILESIZE).getLongValue("value"));
           picMetaInfo.setFormat(metaJson.getJSONObject(ALI_FORMAT).getString("value"));
           picMetaInfo.setHeight(metaJson.getJSONObject(ALI_HEIGHT).getIntValue("value"));
           picMetaInfo.setHeight(metaJson.getJSONObject(ALI_WIDTH).getIntValue("value"));

           return picMetaInfo;
       }
    }
}
