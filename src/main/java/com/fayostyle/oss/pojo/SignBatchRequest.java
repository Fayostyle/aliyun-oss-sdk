package com.fayostyle.oss.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author keith.huang
 * @date 2019/1/14 14:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignBatchRequest {

    private String bizId;
    private String accessType;
    private String httpMethod;
    private List<UniqueReq> uniqueReqs;

    @Data
    @NoArgsConstructor
    public static class UniqueReq {
        private String path;
        private String signature;
        private String ossProcess;
        private Long expire;
        private String responseContentDisposition;
    }
}
