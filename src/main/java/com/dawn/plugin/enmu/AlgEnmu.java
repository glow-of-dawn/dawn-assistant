package com.dawn.plugin.enmu;

/**
 * [加密映射]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
public enum AlgEnmu {

    /* [once] */
    ONCE("once", "once"),
    /* [hash-sign] */
    HASH_SIGN("hash-sign", "hash-sign"),
    /* [hash-type] */
    HASH_TYPE("hash-type", "hash-type"),
    /* [AES] */
    AES("AES", "AES/CBC/PKCS5Padding"),
    /* [-----] */
    SLIGHTLY_5("-----", "5x-"),
    /* [DES] */
    DES("DES", "-"),
    /* [RSA] */
    RSA("RSA", "RSA/ECB/PKCS1Padding"),
    /* [CBC] */
    CBC("CBC", "-"),
    /* [SM1] */
    SM1("SM1", "-"),
    /* [SM2] */
    SM2("SM2", "-"),
    /* [SM3] */
    SM3("SM3", "-"),
    /* [SM4] */
    SM4("SM4", "CBC"),
    /* [MD5] */
    MD5("MD5", "--"),
    /* [MD5] */
    HMACMD5("HmacMD5", "--"),
    /* [SHA-256] */
    SHA256("SHA-256", "--"),
    /* [SHA-512] */
    SHA512("SHA-512", "--"),
    /* [sign] */
    SIGNATURE("sign", "signature"),
    /* [BASE64] */
    BASE64("BASE64", "BASE64"),
    /* [algorithm] */
    ALGORITHM("algorithm", "--"),
    /* [algorithm-iface] */
    ALGORITHM_IFACE("algorithm-iface", "algorithm-interface"),
    /* [key] */
    ALGORITHM_KEY("algorithm-key", "key"),
    /* [algorithm-iv] */
    ALGORITHM_IV("algorithm-iv", "--"),
    /* [algorithm-encoding] */
    ALGORITHM_ENCODING("algorithm-encoding", "--"),
    /* [algorithm-map] */
    ALGORITHM_MAP("algorithm-map", "--");

    private final String algorithm;
    private final String transformation;

    AlgEnmu(String algorithm, String transformation) {
        this.algorithm = algorithm;
        this.transformation = transformation;
    }

    public static String getTransformation(String algorithm) {
        if (algorithm == null) {
            return VarEnmu.NONE.value();
        }
        for (AlgEnmu ele : values()) {
            if (ele.algorithm().equals(algorithm)) {
                return ele.transformation();
            }
        }
        return VarEnmu.NONE.value();
    }

    /**
     * [algorithm]
     *
     * @return java.lang.String
     */
    public String algorithm() {
        return this.algorithm;
    }

    /**
     * [transformation]
     *
     * @return java.lang.String
     */
    public String transformation() {
        return this.transformation;
    }

}
