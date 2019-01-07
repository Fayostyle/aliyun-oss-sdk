package com.fayostyle.oss;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.FileUtil;
import com.aliyun.oss.HttpMethod;
import com.fayostyle.oss.support.ByteOutputStreamWrapper;
import com.fayostyle.oss.support.FileTypePredication;
import com.fayostyle.oss.util.FileUtils;
import com.fayostyle.oss.util.StringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author keith.huang
 * @date 2019/1/4 16:45
 */
@Slf4j
public class DefaultOssClient {

    private CredentialProvider credentialProvider;

    private RemoteSigner remoteSigner = new RemoteSigner();

    public DefaultOssClient(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public DefaultOssClient(String bizId, String secretKey) {
        this.credentialProvider = new CredentialProvider(bizId, secretKey);
    }

    /**
     * 通过指定本地文件路径进行文件上传
     * @param accessType  bucket访问类型，公有或者私有，取值：public private
     * @param uploadPath  上传文件至OSS的存储路径（带文件名，包括后缀）
     * @param localFilePath 将要上传的文件在本地的存储路径
     * @param serverHost    签名授权服务的ip地址和端口
     */
    public void uploadFile(String accessType, String uploadPath, String localFilePath, String serverHost) {
        validate(accessType, uploadPath, serverHost);

        try(FileInputStream inputStream = new FileInputStream(localFilePath)) {
            uploadByStream(accessType, uploadPath, inputStream, serverHost);
        } catch (FileNotFoundException e) {
            log.error("文件找不到， 路径:[{}]", localFilePath);
        } catch (IOException e) {
            log.error("IOException, 读取文件路径[{}]异常", localFilePath);
        }
    }

    /**
     * 通过二进制流上传文件
     * @param accessType  bucket访问类型，公有或者私有，取值：public private
     * @param uploadPath  传文件至OSS的存储路径（带文件名，包括后缀）
     * @param inputStream  上传文件的二进制流
     * @param serverHost  签名授权服务的ip地址和端口
     */
    public void uploadByStream(String accessType, String uploadPath, InputStream inputStream, String serverHost)
            throws IOException {
        validate(accessType, uploadPath, serverHost);

        Preconditions.checkNotNull(inputStream, "上传文件inputStream对象为空");
        try {
            ByteOutputStreamWrapper output = FileUtils.transfer2MemoryOutputStream(inputStream);
            uploadByBytes(accessType, uploadPath, output.acquireZeroCopyBuf(), serverHost);
        } catch (IOException e) {
            log.error("IOException, 从文件流读取文件异常，uploadPath:[{}]", uploadPath);
            throw new RuntimeException("IOException, 从文件流读取文件异常，uploadPath：" + uploadPath);
        } finally {
            inputStream.close();
        }
    }

    /**
     * 通过二进制数组上传，
     * @param accessType
     * @param uploadPath
     * @param file
     * @param serverHost
     */
    public void uploadByBytes(String accessType, String uploadPath, byte[] file, String serverHost) {

        validate(accessType, uploadPath, serverHost);

        Preconditions.checkNotNull(file);

        String literalExtension = StringHelper.getExtensionFromUri(uploadPath);
        String contentType = FileTypePredication.acquireMimeByBytes(file, literalExtension);

        OssContext ossContext = new OssContext(credentialProvider, HttpMethod.PUT.name(), accessType, serverHost);

        ossContext.setFile(file);
        ossContext.setContentType(contentType);
        ossContext.setPath(uploadPath);

        uploadByBytes(ossContext);
    }

    private void uploadByBytes(OssContext ossContext) {
        long startTime = System.currentTimeMillis();

        String signResult = remoteSigner.callCommonAuthSignService(ossContext);
        if(log.isInfoEnabled()) {
            long end = System.currentTimeMillis();
            log.info("本地签名和远程认证耗时, {}, path,{}", end - startTime, ossContext.getPath());
        }



    }

    private void validate(String accessType, String uploadPath, String serverHost) {
        if(Strings.isNullOrEmpty(accessType) || StringHelper.ACCESS_TYPES.indexOf(accessType.toLowerCase()) == -1) {
            throw new OssException("accessType有误");
        }

        if(Strings.isNullOrEmpty(uploadPath)) {
            throw new OssException("path 不能为空");
        }

        if(serverHost.indexOf(StringHelper.COLON) == -1) {
            throw new OssException("serverHost必须包含ip和port， 格式：XX.XX.XX.XX:port");
        }
    }

}
