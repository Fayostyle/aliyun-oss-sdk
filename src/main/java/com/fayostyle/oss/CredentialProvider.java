package com.fayostyle.oss;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author keith.huang
 * @date 2019/1/5 18:28
 */
@Data
@AllArgsConstructor
public class CredentialProvider {

    private String bizId;

    private String secretKey;
}
