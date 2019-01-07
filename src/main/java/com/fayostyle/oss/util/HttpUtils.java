package com.fayostyle.oss.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/**
 * @author keith.huang
 * @date 2019/1/7 17:38
 */
@Slf4j
public class HttpUtils {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static String doGet(String url) {

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            return response.body().string();
        } catch (IOException e) {
            log.error("调用OSS签名授权服务异常：{}", e.getMessage());
            throw new RuntimeException("调用OSS签名授权服务异常：" + e.getMessage());
        }
    }

    public static String doPost(String url, String json) {
        if (log.isInfoEnabled()) {
            log.info("发送给签名服务的json，{}，url，{}", json, url);
        }

        OkHttpClient client = new OkHttpClient();

        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, json)).build();

        try (Response response = client.newCall(req).execute()) {
            return response.body().string();
        } catch (IOException e) {
            log.error("调用OSS签名授权服务异常：{}", e.getMessage());
            throw new RuntimeException("调用OSS签名授权服务异常：" + e.getMessage());
        }
    }
}
