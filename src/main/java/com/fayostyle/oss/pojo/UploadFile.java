package com.fayostyle.oss.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author keith.huang
 * @date 2019/1/9 20:43
 */
@Data
@AllArgsConstructor
public class UploadFile {
    private String accessType;
    private String uploadPaht;
    private String serverHost;
    private byte[] file;
    private String fileNameAlias;

}
