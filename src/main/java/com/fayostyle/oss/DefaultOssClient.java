package com.fayostyle.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.fayostyle.oss.pojo.UploadFile;
import com.fayostyle.oss.support.ByteOutputStreamWrapper;
import com.fayostyle.oss.support.FileTypePredication;
import com.fayostyle.oss.util.FileUtils;
import com.fayostyle.oss.util.StringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author keith.huang
 * @date 2019/1/4 16:45
 */
@Slf4j
public class DefaultOssClient {

    private static final int MAX_UPLOAD_FILE_COUNT = 10;

    private CredentialProvider credentialProvider;

    private RemoteSigner remoteSigner = new RemoteSigner();

    private static ExecutorService executorPool = new ThreadPoolExecutor(4,
            10 , 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());



    public DefaultOssClient(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public DefaultOssClient(String bizId, String secretKey) {
        this.credentialProvider = new CredentialProvider(bizId, secretKey);
    }

    /**
     * 通过指定本地文件路径进行文件上传
     *
     * @param accessType    bucket访问类型，公有或者私有，取值：public private
     * @param uploadPath    上传文件至OSS的存储路径（带文件名，包括后缀）
     * @param localFilePath 将要上传的文件在本地的存储路径
     * @param serverHost    签名授权服务的ip地址和端口
     */
    public void uploadFile(String accessType, String uploadPath, String localFilePath, String serverHost) {
        validate(accessType, uploadPath, serverHost);

        try (FileInputStream inputStream = new FileInputStream(localFilePath)) {
            uploadByStream(accessType, uploadPath, inputStream, serverHost);
        } catch (FileNotFoundException e) {
            log.error("文件找不到， 路径:[{}]", localFilePath);
        } catch (IOException e) {
            log.error("IOException, 读取文件路径[{}]异常", localFilePath);
        }
    }

    /**
     * 通过二进制流上传文件
     *
     * @param accessType  bucket访问类型，公有或者私有，取值：public private
     * @param uploadPath  传文件至OSS的存储路径（带文件名，包括后缀）
     * @param inputStream 上传文件的二进制流
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
     *
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

    public void uploadBytesByFileAlias(String accessType, String uploadPath, byte[] file, String fileAlias, String serverHost) {
        validate(accessType, uploadPath, serverHost);

        Preconditions.checkNotNull(file);

        String literalExtension = StringHelper.getExtensionFromUri(uploadPath);
        String contentType = FileTypePredication.acquireMimeByBytes(file, literalExtension);

        OssContext ossContext = new OssContext(credentialProvider, HttpMethod.PUT.name(), accessType, serverHost);
        ossContext.setFile(file);
        ossContext.setContentType(contentType);
        ossContext.setPath(uploadPath);
        ossContext.setFileNameAlias(fileAlias);

        uploadByBytes(ossContext);
    }

    private void uploadByBytes(OssContext ossContext) {
        long startTime = System.currentTimeMillis();

        String signResult = remoteSigner.callCommonAuthSignService(ossContext);
        if (log.isInfoEnabled()) {
            long end = System.currentTimeMillis();
            log.info("本地签名和远程认证耗时, {}, path,{}", end - startTime, ossContext.getPath());
        }

        SignerResultParser signedResultParser = SignerResultParser.create().parse(signResult);
        if (signedResultParser.isResponseSuccess()) {
            System.out.println(signedResultParser.getSignedUrl());

            Map<String, String> reqHeaders = Maps.newHashMapWithExpectedSize(2);
            reqHeaders.put("Content-Type", ossContext.decodeContentType());

            String alias = ossContext.getFileNameAlias();
            if (!Strings.isNullOrEmpty(alias)) {
                try {
                    reqHeaders.put("Content-Disposition", "attachment;filename=" + URLEncoder.encode(alias, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            doOssUpload(signedResultParser, ossContext.getFile(), reqHeaders);
        } else {
            String error = signedResultParser.getMessage();
            log.error("调用签名服务异常，错误信息{}", error);
            throw new OssException(error);
        }

        if (log.isInfoEnabled()) {
            long end = System.currentTimeMillis();
            log.info("上传到oss总耗时：[{}], path:[{}]", end - startTime, ossContext.getPath());
        }
    }

    /**
     * 异步上传
     * @param accessType
     * @param uploadPath
     * @param file
     * @param serverHost
     */
    public void asyncUploadByBytes(String accessType, String uploadPath, byte[] file, String serverHost) {
        validate(accessType, uploadPath, serverHost);

        AsyncOssUploadTask asyncOssUploadTask = new AsyncOssUploadTask(accessType, file, serverHost, uploadPath);

        executorPool.execute(asyncOssUploadTask);

    }

    /**
     * 异步批量上传
     * @param files
     */
    public void asyncBatchUploadByBytes(List<UploadFile> files) {
        Preconditions.checkArgument(files != null, "批量上传文件，参数files不能为空");
        Preconditions.checkArgument(files.size() < MAX_UPLOAD_FILE_COUNT,
                "异步批量上传文件数不能超过"+ MAX_UPLOAD_FILE_COUNT);

        if(files.size() == 0) {
            return;
        }

        for(UploadFile file : files) {
            asyncUploadByBytes(file.getAccessType(), file.getUploadPaht(), file.getFile(), file.getServerHost());
        }
    }




    private void doOssUpload(SignerResultParser signerResultParser, byte[] file, Map<String, String> reqHeader) {
        long start = System.currentTimeMillis();

        URL url = null;
        try {
            url = new URL(signerResultParser.getSignedUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        OSSClient ossClient = new OSSClient("endpoint.com", "accesskey", "secretkey");
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(file);
        ossClient.putObject(url, byteInputStream, file.length, reqHeader);
        ossClient.shutdown();

        long end = System.currentTimeMillis();
        log.info("上传到oss实际耗时：{}, signedUrl", end - start, signerResultParser.getSignedUrl());
    }

    /**
     * 下载文件
     * @param accessType
     * @param path
     * @param expire 持下载链接有效的时间段，单位：秒。如果给定null，则按300秒设定。
     * @param serverHost
     * @return
     */
    public byte[] downLoadAsBytes(String accessType, String path, Long expire, String serverHost) {
        validate(accessType, path, serverHost);

        OssContext ossContext = new OssContext(credentialProvider, HttpMethod.GET.name(), accessType, serverHost);
        ossContext.setExpire(expire);
        ossContext.setPath(path);

        return downLoadAsBytes(ossContext);

    }

    public byte[] downLoadAsBytes(OssContext ossContext) {
        long start = System.currentTimeMillis();

        String signResult = remoteSigner.callCommonAuthSignService(ossContext);
        return null;
    }

    private void validate(String accessType, String uploadPath, String serverHost) {
        if (Strings.isNullOrEmpty(accessType) || StringHelper.ACCESS_TYPES.indexOf(accessType.toLowerCase()) == -1) {
            throw new OssException("accessType有误");
        }

        if (Strings.isNullOrEmpty(uploadPath)) {
            throw new OssException("path 不能为空");
        }

        if (serverHost.indexOf(StringHelper.COLON) == -1) {
            throw new OssException("serverHost必须包含ip和port， 格式：XX.XX.XX.XX:port");
        }
    }

    @AllArgsConstructor
    private class AsyncOssUploadTask implements Runnable {
        private String accessType;
        private byte[] file;
        private String serverHost;
        private String uploadPath;

        @Override
        public void run() {
            try {
                uploadByBytes(accessType, uploadPath, file, serverHost);
            } catch (Exception e) {
                log.error("异步上传失败,accessType:[{}], path:[{}], 信息：[{}]", accessType, uploadPath, e.getMessage());
            }
        }
    }


    @Data
    private static class SignerResultParser {

        private final static String STATUS = "status";
        private final static String SIGNED_URL = "signedUrl";
        private final static String MESSAGE = "message";
        private final static String SUCCESS = "success";

        private String status;
        private String message;
        private String signedUrl;

        public static SignerResultParser create() {
            return new SignerResultParser();
        }

        public SignerResultParser parse(String signResult) {
            if (Strings.isNullOrEmpty(signResult)) {
                throw new RuntimeException("签名服务授权返回为空");
            }

            JSONObject resultJson = JSON.parseObject(signResult);
            this.message = resultJson.getString(MESSAGE);
            this.status = resultJson.getString(STATUS);
            this.signedUrl = resultJson.getString(SIGNED_URL);

            return this;
        }

        public boolean isResponseSuccess() {
            return SUCCESS.equalsIgnoreCase(status) ? true : false;
        }
    }

    public enum HttpMethod {
        PUT, GET, HEAD;
    }

}
