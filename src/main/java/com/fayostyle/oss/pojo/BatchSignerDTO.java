package com.fayostyle.oss.pojo;

import com.fayostyle.oss.OssException;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author keith.huang
 * @date 2019/1/14 10:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchSignerDTO {

    private String accessType;
    private String serverHost;
    private List<BatchInfo> batchInfos;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchInfo {

        private String path;
        private long expire;
        private String fileNameAlias;
        private String imgStyle;

        public BatchInfo(String path) {
            this.path = path;
        }

        public BatchInfo(String path, String imgStyle) {
            this.path = path;
            this.imgStyle = imgStyle;
        }
    }

    public void validateNotEmpty() {
        if(Strings.isNullOrEmpty(accessType)) {
            throw new OssException("BatchSignerDTO中accessType不能为空");
        }

        if(Strings.isNullOrEmpty(serverHost)) {
            throw new OssException("BatchSignerDTO中serverHost不能为空");
        }

        if(batchInfos.size() == 0 || batchInfos == null) {
            throw new OssException("BatchSignerDTO中List<BatchInfo>不能为空");
        }

        for(BatchInfo batchInfo : batchInfos) {
            if(Strings.isNullOrEmpty(batchInfo.getPath())) {
                throw new OssException("BatchSignerDTO中batchinfo的path一个都不能为空");
            }
        }
    }
}
