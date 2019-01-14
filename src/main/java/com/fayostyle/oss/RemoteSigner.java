package com.fayostyle.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fayostyle.oss.pojo.BatchSignerDTO;
import com.fayostyle.oss.pojo.SignBatchRequest;
import com.fayostyle.oss.support.SignAlgorithmContext.*;
import com.fayostyle.oss.support.*;
import com.fayostyle.oss.util.HttpUtils;
import com.fayostyle.oss.util.StringHelper;
import com.fayostyle.oss.util.SignUtils;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author keith.huang
 * @date 2019/1/7 10:42
 */
public class RemoteSigner {

    public static final String SCHEMA = "http://";
    public static final String BIZ_ID = "bizId";
    public static final String PATH = "path";
    public final static String HTTP_METHOD="httpMethod";
    public final static String ACCESS_TYPE="accessType";
    public final static String CONTENT_TYPE="contentType";
    public final static String SIGNATURE="signature";
    public final static String BATCH_SIGN_URI = "/permission/signAll";
    public final static String SIGN_URI = "/permission/sign";

    public final static String IMG_OSS_IDENTITY = "ossProcess";
    public final static String RESPONSE_CONTENT_DISPOSITION = "responseContentDisposition";
    public final static String EXPIRE_IDENTITY = "expire";

    public final static String PARTIAL_CONTENT_DISPOSITION = "attachment;filename=";


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

        return HttpUtils.doGet(restfulAPI);
    }

    public String batchCallCommonAuthSignService(BatchSignerDTO batchSignerDTO, CredentialProvider provider) {

        validate(batchSignerDTO);

        SignBatchRequest signBatchRequest = new SignBatchRequest();
        signBatchRequest.setAccessType(batchSignerDTO.getAccessType());
        signBatchRequest.setBizId(provider.getBizId());
        signBatchRequest.setHttpMethod("GET");

        List<BatchSignerDTO.BatchInfo> infos = batchSignerDTO.getBatchInfos();
        for(BatchSignerDTO.BatchInfo info : infos) {
            String signature = createSignature(provider, batchSignerDTO.getAccessType(), info.getPath());
            SignBatchRequest.UniqueReq uniqueReq = new SignBatchRequest.UniqueReq();

            uniqueReq.setExpire(info.getExpire());
            uniqueReq.setPath(info.getPath());
            uniqueReq.setSignature(signature);
            uniqueReq.setOssProcess(info.getImgStyle());

            String fileNameAlias = info.getFileNameAlias();
            if(!Strings.isNullOrEmpty(fileNameAlias)) {
                uniqueReq.setResponseContentDisposition(fileNameAlias);
            }

            signBatchRequest.getUniqueReqs().add(uniqueReq);
        }

        String url = SCHEMA + batchSignerDTO.getServerHost() + BATCH_SIGN_URI;

        return HttpUtils.doPost(url, JSONObject.toJSONString(signBatchRequest));
    }

    private String createSignature(OssContext ossContext, boolean isUrlEncoded) {
        CredentialProvider provider = ossContext.getCredentialProvider();

        String canonicalString = SignUtils.buildLocalCanonicalString(provider.getBizId(),
                ossContext.getAccessType(), ossContext.getPath());

        SignAlgorithmContext signAlgorithmContext = SignAlgorithmContext.create(SignAlgorithm.HMAC_SHA1);
        String signature = signAlgorithmContext.computeSignature(provider.getSecretKey(), canonicalString);

        if(isUrlEncoded) {
            String encodeSignature = "";
            try {
                encodeSignature = URLEncoder.encode(encodeSignature, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return encodeSignature;

        } else {
            return signature;
        }

    }

    private String createSignature(CredentialProvider provider, String accessType, String path) {
        OssContext ossContext = new OssContext(provider, accessType, path);

        return createSignature(ossContext, false);

    }

    public void validate(BatchSignerDTO signerDTO) {
        signerDTO.validateNotEmpty();
    }

    private String buildAccessURL(OssContext ossContext) {

        StringBuilder sb = new StringBuilder();

        sb.append(SCHEMA).append(ossContext.getServerHost()).append(SIGN_URI)
                .append(StringHelper.QUERY_STRING_DILIMETER)
                .append(BIZ_ID).append(StringHelper.EQUAL).append(ossContext.getCredentialProvider().getBizId())
                .append(StringHelper.AND)
                .append(HTTP_METHOD).append(StringHelper.EQUAL).append(ossContext.getHttpMethod())
                .append(StringHelper.AND)
                .append(ACCESS_TYPE).append(StringHelper.EQUAL).append(ossContext.getAccessType())
                .append(StringHelper.AND)
                .append(CONTENT_TYPE).append(StringHelper.EQUAL).append(ossContext.getContentType())
                .append(StringHelper.AND)
                .append(PATH).append(StringHelper.EQUAL).append(ossContext.getPath())
                .append(StringHelper.AND)
                .append(SIGNATURE).append(StringHelper.EQUAL).append(ossContext.getSignature());

        if(ossContext.isImageStyleNotEmpty()) {
            sb.append(StringHelper.AND)
                    .append(IMG_OSS_IDENTITY).append(StringHelper.EQUAL).append(ossContext.getImageStyle());
        }

        if(ossContext.isExpireNotNull()) {
            sb.append(StringHelper.AND)
                    .append(EXPIRE_IDENTITY).append(StringHelper.EQUAL).append(ossContext.getExpire());
        }

        if(ossContext.isFileNameAliasNotNull()) {
            sb.append(StringHelper.AND)
                    .append(RESPONSE_CONTENT_DISPOSITION).append(StringHelper.EQUAL)
                    .append(wrapFileName2ContentDisposition(ossContext.getFileNameAlias()));
        }

        return sb.toString();

    }

    private String wrapFileName2ContentDisposition(String fileNameAlias) {
        String defaultName = "文件";
        try {
            return PARTIAL_CONTENT_DISPOSITION + URLEncoder.encode(fileNameAlias, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return PARTIAL_CONTENT_DISPOSITION + defaultName;
        }
    }
}
