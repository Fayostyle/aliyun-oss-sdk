package com.fayostyle.oss;

import com.fayostyle.oss.support.SignAlgorithmContext.*;
import com.fayostyle.oss.support.*;
import com.fayostyle.oss.util.StringHelper;
import com.fayostyle.oss.util.SignUtils;
/**
 * @author keith.huang
 * @date 2019/1/7 10:42
 */
public class RemoteSigner {

    public String callCommonAuthSignService(OssContext ossContext) {
        String path = ossContext.getPath();
        if(!path.startsWith(StringHelper.SLASH)) {
            path = StringHelper.SLASH + path;
            ossContext.setPath(path);
        }

        String signature = createSignature(ossContext, true);

        ossContext.setSignature(signature);
        ossContext.safeTransportByUrlencode();

        String restfulAPI = buildAccessURL(ossContext);
    }

    private String createSignature(OssContext ossContext, boolean isUrlEncoded) {
        CredentialProvider provider = ossContext.getCredentialProvider();

        String canonicalString = SignUtils.buildLocalCanonicalString(provider.getBizId(),
                ossContext.getAccessType(), ossContext.getPath());

        SignAlgorithmContext signAlgorithmContext = SignAlgorithmContext.create(SignAlgorithm.HMAC_SHA1);

    }

    private String buildAccessURL(OssContext ossContext) {

    }
}
