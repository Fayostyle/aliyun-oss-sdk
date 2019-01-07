package com.fayostyle.oss.support;


/**
 * @author keith.huang
 * @date 2019/1/7 14:10
 */
public abstract class SignAlgorithmContext {

    public abstract String getAlgorithm();

    public abstract String getVersion();

    /**
     * 计算签名
     * @param key 签名所需的秘钥，对应于访问的access key
     * @param data 用于计算签名的字符串信息
     * @return 签名字符串
     */
    public abstract String computeSignature(String key, String data);

    public static SignAlgorithmContext create(SignAlgorithm algorithm) {
        return algorithm.getAlgorithm();
    }

    public enum SignAlgorithm {
        HMAC_SHA1(new HmacSHA1Signature());

        private SignAlgorithmContext algorithm;

        SignAlgorithm(SignAlgorithmContext context) {
            this.algorithm = context;
        }

        public SignAlgorithmContext getAlgorithm() {
            return this.algorithm;
        }
    }
}
