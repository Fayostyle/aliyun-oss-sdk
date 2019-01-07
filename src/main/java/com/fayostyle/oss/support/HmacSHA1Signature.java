package com.fayostyle.oss.support;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author keith.huang
 * @date 2019/1/7 14:28
 */
public class HmacSHA1Signature extends SignAlgorithmContext {

    public HmacSHA1Signature() {
        System.out.println("enter..");
    }

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String ALGORITHM = "HmacSHA1";

    private static final String VERSION = "1";

    private static final Object LOCK = new Object();

    private static Mac macInstance;



    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String computeSignature(String key, String data) {
        try {
            byte[] signData = sign(key.getBytes(DEFAULT_ENCODING), data.getBytes(DEFAULT_ENCODING));
            return Base64.encodeBase64String(signData);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] sign(byte[] keyBytes, byte[] dataBytes) {
        try {
            if (macInstance == null) {
                synchronized (LOCK) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(ALGORITHM);
                    }
                }
            }

            Mac mac = null;
            try {
                mac = (Mac) macInstance.clone();
            } catch (CloneNotSupportedException e) {
                mac = Mac.getInstance(ALGORITHM);
            }

            mac.init(new SecretKeySpec(keyBytes, ALGORITHM));
            return mac.doFinal(dataBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
