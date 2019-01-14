package com.fayostyle.oss.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author keith.huang
 * @date 2019/1/14 10:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PermissionVO {

    private String path;
    private String status;
    private String signedUrl;
    private String message;
}
