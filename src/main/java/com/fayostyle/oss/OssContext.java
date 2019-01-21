package com.fayostyle.oss;

import com.fayostyle.oss.util.StringHelper;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * @author keith.huang
 * @date 2019/1/5 18:27
 */
@Data
@AllArgsConstructor
public class OssContext {

    private CredentialProvider credentialProvider;
    private String httpMethod;
    private String accessType;
    private String path;
    private Long expire;

    private String serverHost;

    private String signature;

    private String contentType;

    private String imageStyle;

    private byte[] file;

    private String fileNameAlias;

    public OssContext(CredentialProvider provider, String httpMethod, String accessType, String serverHost) {
        this.credentialProvider = provider;
        this.httpMethod = httpMethod;
        this.accessType = accessType;
        this.serverHost = serverHost;
    }

    public OssContext(CredentialProvider provider, String accessType, String path) {
          this.path = path;
          this.credentialProvider = provider;
          this.accessType = accessType;
    }

    public void safeTransportByUrlencode() {
        try {
            if (!Strings.isNullOrEmpty(path)) {
                path = URLEncoder.encode(path, "UTF-8");
            }

            if(!Strings.isNullOrEmpty(contentType)) {
                contentType = URLEncoder.encode(contentType, "UTF-8");
            }

            if(!Strings.isNullOrEmpty(imageStyle)) {
                imageStyle = URLEncoder.encode(imageStyle, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {

        }
    }

    public String decodeContentType() {
        contentType = StringHelper.urlDecode(contentType);
        return contentType;
    }

    public boolean isImageStyleNotEmpty() {
        return !Strings.isNullOrEmpty(imageStyle);
    }

    public boolean isExpireNotNull() {
        return !Objects.isNull(expire);
    }

    public boolean isFileNameAliasNotNull() {
        return !Objects.isNull(fileNameAlias);
    }


}
