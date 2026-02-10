package com.dawn.plugin.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * [加解密处理]
 * 创建时间：2021/2/2 14:00
 *
 * @author hforest-480s
 */
@Slf4j
public class CrypUtil {

    private CrypUtil() {
    }

    /**
     * [加密des]
     *
     * @param key     [8位]
     * @param message [message]
     * @return String
     */
    public static String encrypDesBase64(String key, String message) {
        return encrypBase64(key, key, message, "DES/CBC/PKCS5Padding", "DES", VarEnmu.UTF8.value());
    }

    /**
     * [加密解密des]
     *
     * @param key     [8位]
     * @param message [message]
     * @return String
     */
    public static String decodeDesBase64(String key, String message) {
        return decodeBase64(key, key, message, "DES/CBC/PKCS5Padding", "DES", VarEnmu.UTF8.value());
    }

    /**
     * [加密aes]
     *
     * @param key     [16位]
     * @param message [message]
     * @return java.lang.String
     **/
    public static String encrypAesBase64(String key, String message) {
        return encrypBase64(key, key, message, "AES/CBC/PKCS5Padding", "AES", VarEnmu.UTF8.value());
    }

    /**
     * [解密aes]
     *
     * @param key     [16位]
     * @param message [message]
     * @return java.lang.String
     **/
    public static String decodeAesBase64(String key, String message) {
        return decodeBase64(key, key, message, "AES/CBC/PKCS5Padding", "AES", VarEnmu.UTF8.value());
    }

    /**
     * [加密SM4]
     *
     * @param key     [16位]
     * @param message [message]
     * @return java.lang.String
     **/
    public static String encrypSm4Base64(String key, String message) {
        return encrypBase64(key, key, message, "CBC", "SM4", VarEnmu.UTF8.value());
    }

    /**
     * [解密SM4]
     *
     * @param key     [16位]
     * @param message [message]
     * @return java.lang.String
     **/
    public static String decodeSm4Base64(String key, String message) {
        return decodeBase64(key, key, message, "CBC", "SM4", VarEnmu.UTF8.value());
    }

    /**
     * [解密]
     *
     * @param key            [16位 / 8位]
     * @param iv             [16位 / 8位]
     * @param message        [message]
     * @param transformation [AES/CBC/PKCS5Padding]:[algorithm:AES] [DES/CBC/PKCS5Padding]:[algorithm:DES] [ZeroPadding]:[CBC]
     * @param algorithm      [AES] [DES] [CBC]
     * @param encoding       [encoding]
     * @return java.lang.String
     **/
    public static String decodeBase64(String key, String iv, String message, String transformation, String algorithm, String encoding) {
        switch (algorithm.concat(".").concat(transformation)) {
            case "SM4.CBC":
                /* def CBC */
                return decodeBase64BySm4Cbc(key, iv, message, Padding.PKCS5Padding, encoding);
            case "AES.CBC":
                /* def CBC */
                return decodeBase64ByWorld(key, iv, message, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), encoding);
            default:
                return decodeBase64ByWorld(key, iv, message, transformation, algorithm, encoding);
        }
    }

    /**
     * [解密]
     *
     * @param key            [16位 / 8位]
     * @param iv             [16位 / 8位]
     * @param message        [message]
     * @param transformation [AES/CBC/PKCS5Padding]:[algorithm:AES] [DES/CBC/PKCS5Padding]:[algorithm:DES]
     * @param algorithm      [AES] [DES]
     * @param encoding       [encoding]
     * @return java.lang.String
     **/
    public static String decodeBase64ByWorld(String key, String iv, String message, String transformation, String algorithm, String encoding) {
        String result = VarEnmu.NONE.value();
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(encoding));
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            /* 与加密时不同MODE:Cipher.DECRYPT_MODE */
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] datasource = Base64.decodeBase64(message);
            byte[] str = cipher.doFinal(datasource);
            result = new String(str, encoding);
        } catch (IOException
                 | InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException
                 | InvalidAlgorithmParameterException e) {
            log.error(LogEnmu.LOG6.value(), "encrypBase64", message, transformation, algorithm, encoding, e.toString());
        }
        return result;
    }

    /**
     * [解密]
     * transformation [ZeroPadding:CBC]
     * algorithm [CBC]
     *
     * @param key      [16位 / 8位]
     * @param iv       [16位 / 8位]
     * @param message  [message]
     * @param padding  [Padding.PKCS5Padding]
     * @param encoding [encoding]
     * @return java.lang.String
     **/
    public static String decodeBase64BySm4Cbc(String key, String iv, String message, Padding padding, String encoding) {
        String result = VarEnmu.NONE.value();
        try {
            SymmetricCrypto sm4 = new SM4(Mode.CBC, padding, key.getBytes(Charset.forName(encoding)), iv.getBytes(Charset.forName(encoding)));
            byte[] cipherHex = cn.hutool.core.codec.Base64.decode(message);
            result = sm4.decryptStr(cipherHex, Charset.forName(encoding));
        } catch (Exception ex) {
            log.error(LogEnmu.LOG5.value(), "decodeBase64BySm4Cbc.解密失败", message, padding, encoding, ex.toString());
        }
        return result;
    }

    /**
     * [加密aes]
     *
     * @param key            [16位 / 8位]
     * @param iv             [16位 / 8位]
     * @param message        [message]
     * @param transformation [AES/CBC/PKCS5Padding]:[algorithm:AES] [DES/CBC/PKCS5Padding]:[algorithm:DES] [ZeroPadding]:[CBC]
     * @param algorithm      [AES] [DES] [CBC]
     * @param encoding       [encoding]
     * @return java.lang.String
     **/
    public static String encrypBase64(String key, String iv, String message, String transformation, String algorithm, String encoding) {
        switch (algorithm.concat(".").concat(transformation)) {
            case "SM4.CBC":
                /* def CBC */
                return encrypBase64BySm4Cbc(key, iv, message, Padding.PKCS5Padding, encoding);
            case "AES.CBC":
                /* def CBC */
                return encrypBase64ByWorld(key, iv, message, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), encoding);
            default:
                return encrypBase64ByWorld(key, iv, message, transformation, algorithm, encoding);
        }
    }

    /**
     * [加密sm4]
     * transformation [ZeroPadding:CBC]
     * algorithm [CBC]
     *
     * @param key      [16位 / 8位]
     * @param iv       [16位 / 8位]
     * @param message  [message]
     * @param encoding [encoding]
     * @param padding  [Padding.PKCS5Padding]
     * @return java.lang.String
     **/
    public static String encrypBase64BySm4Cbc(String key, String iv, String message, Padding padding, String encoding) {
        String result = VarEnmu.NONE.value();
        try {
            SymmetricCrypto sm4 = new SM4(Mode.CBC, padding, key.getBytes(Charset.forName(encoding)), iv.getBytes(Charset.forName(encoding)));
            byte[] encrypHe = sm4.encrypt(message);
            result = cn.hutool.core.codec.Base64.encode(encrypHe);
        } catch (Exception ex) {
            log.error(LogEnmu.LOG5.value(), "encrypBase64BySm4Cbc.加密失败", message, padding, encoding, ex.toString());
        }
        return result;
    }

    /**
     * [加密aes]
     *
     * @param key            [16位 / 8位]
     * @param iv             [16位 / 8位]
     * @param message        [message]
     * @param transformation [AES/CBC/PKCS5Padding]:[algorithm:AES] [DES/CBC/PKCS5Padding]:[algorithm:DES]
     * @param algorithm      [AES] [DES]
     * @param encoding       [encoding]
     * @return java.lang.String
     **/
    public static String encrypBase64ByWorld(String key, String iv, String message, String transformation, String algorithm, String encoding) {
        String result = VarEnmu.NONE.value();
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(encoding));
            /* 两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES */
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(encoding), algorithm);
            /* 实例化加密类，PKCS5Padding比PKCS7Padding效率高，PKCS7Padding可支持IOS加解密 */
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] desBytes = cipher.doFinal(message.getBytes(encoding));
            /* BASE64编码阶段 */
            result = Base64.encodeBase64String(desBytes);
        } catch (InvalidKeyException e) {
            log.error(LogEnmu.LOG6.value(), "encrypBase64.1", message, transformation, algorithm, encoding, e.toString());
        } catch (NoSuchAlgorithmException
                 | UnsupportedEncodingException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException
                 | InvalidAlgorithmParameterException e) {
            log.error(LogEnmu.LOG6.value(), "encrypBase64.2", message, transformation, algorithm, encoding, e.toString());
        }
        return result;
    }

    /**
     * [生成SM2公私钥]
     *
     * @return Map<String, String>
     **/
    public static Map<String, String> generateSm2Key() {
        KeyPair pair = SecureUtil.generateKeyPair(AlgEnmu.SM2.algorithm());
        ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();

        /* 获取公钥 */
        byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
        String publicKeyHex = HexUtil.encodeHexStr(publicKeyBytes);

        /* 获取64位私钥 */
        String privateKeyHex = privateKey.getD().toString(VarEnmu.SIXTEEN.ivalue());
        /* BigInteger转成16进制时，不一定长度为64，如果私钥长度小于64，则在前方补0 */
        StringBuilder privateKey64 = new StringBuilder(privateKeyHex);
        while (privateKey64.length() < VarEnmu.NUMBER_64.ivalue()) {
            privateKey64.insert(VarEnmu.ZERO.ivalue(), VarEnmu.ZERO.value());
        }

        Map<String, String> result = new HashMap<>();
        result.put(VarEnmu.PUBLIC_KEY.value(), publicKeyHex);
        result.put(VarEnmu.PUBLIC_KEY_HEX.value(), cn.hutool.core.codec.Base64.encode(publicKey.getEncoded()));
        result.put(VarEnmu.PRIVATE_KEY.value(), privateKey64.toString());
        result.put(VarEnmu.PRIVATE_KEY_HEX.value(), cn.hutool.core.codec.Base64.encode(privateKey.getEncoded()));
        return result;
    }

    /**
     * [SM2公钥加密]
     *
     * @param content   [原文]
     * @param publicKey [SM2公钥]
     * @return String
     */
    public static String encrypBase64BySm2(String content, String publicKey) {
        String result = VarEnmu.NONE.value();
        try {
            SM2 sm2 = new SM2(null, publicKey);
            return sm2.encryptBase64(content, KeyType.PublicKey);
        } catch (Exception ex) {
            log.error(LogEnmu.LOG3.value(), "encrypBase64BySm2.加密失败", content, ex.toString());
        }
        return result;
    }

    /**
     * [SM2私钥解密]
     *
     * @param encryptStr [SM2加密字符串]
     * @param privateKey [SM2私钥]
     * @return String
     */
    public static String decodeBase64BySm2(String encryptStr, String privateKey) {
        String result = VarEnmu.NONE.value();
        try {
            SM2 sm2 = new SM2(privateKey, null);
            result = sm2.decryptStr(encryptStr, KeyType.PrivateKey);
        } catch (Exception ex) {
            log.error(LogEnmu.LOG3.value(), "decodeBase64BySm2.解密失败", encryptStr, ex.toString());
        }
        return result;
    }

    /**
     * [SM3哈希]
     *
     * @param contents [原文]
     * @return String
     */
    public static String sm3Hex(String... contents) {
        SM3 sm3 = new SM3();
        var content = String.join(VarEnmu.NONE.value(), contents);
        return sm3.digestHex(content);
    }

    /**
     * [RSA加密]
     *
     * @param plainText       [plainText]
     * @param publicKeyBase64 [publicKeyBase64]
     * @return String
     **/
    public static String encryptBase64ByRsa(String plainText, String publicKeyBase64) {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(publicKeyBase64.replaceAll("\\s", VarEnmu.NONE.value()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(AlgEnmu.RSA.algorithm());
            PublicKey publicKey = kf.generatePublic(spec);

            /* "RSA/ECB/PKCS1Padding" */
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(AlgEnmu.RSA.transformation());
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);

            int keyBitLength = ((java.security.interfaces.RSAPublicKey) publicKey).getModulus().bitLength();
            /* PKCS#1 v1.5 padding */
            int maxBlock = keyBitLength / 8 - 11;

            byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
            try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                for (int offset = VarEnmu.ZERO.ivalue(); offset < data.length; offset += maxBlock) {
                    int len = Math.min(maxBlock, data.length - offset);
                    byte[] block = cipher.doFinal(data, offset, len);
                    out.write(block);
                }
                return java.util.Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (Exception ex) {
            log.error(LogEnmu.LOG3.value(), "rsaEncryptBase64.加密失败", plainText, ex.toString());
            return VarEnmu.NONE.value();
        }
    }

    /**
     * [RSA解密]
     *
     * @param cipherBase64     [cipherBase64]
     * @param privateKeyBase64 [privateKeyBase64]
     * @return String
     **/
    public static String decryptBase64ByRsa(String cipherBase64, String privateKeyBase64) {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(privateKeyBase64.replaceAll("\\s", VarEnmu.NONE.value()));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(AlgEnmu.RSA.algorithm());
            PrivateKey privateKey = kf.generatePrivate(spec);

            /* "RSA/ECB/PKCS1Padding" */
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(AlgEnmu.RSA.transformation());
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedData = java.util.Base64.getDecoder().decode(cipherBase64);
            int keyBitLength = ((java.security.interfaces.RSAPrivateKey) privateKey).getModulus().bitLength();
            /* 密文块大小等于密钥字节长度 */
            int maxBlock = keyBitLength / VarEnmu.EIGHT.ivalue();

            try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                for (int offset = VarEnmu.ZERO.ivalue(); offset < encryptedData.length; offset += maxBlock) {
                    int len = Math.min(maxBlock, encryptedData.length - offset);
                    byte[] block = cipher.doFinal(encryptedData, offset, len);
                    out.write(block);
                }
                return out.toString(StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            log.error(LogEnmu.LOG3.value(), "rsaDecryptBase64.解密失败", cipherBase64, ex.toString());
            return VarEnmu.NONE.value();
        }
    }

    /**
     * [生成RSA公私钥]
     *
     * @param keySize [2048]
     * @return Map<String, String>
     **/
    public static Map<String, String> generateRsaKey(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(AlgEnmu.RSA.algorithm());
        kpg.initialize(keySize);
        KeyPair pair = kpg.generateKeyPair();
        String publicKeyHex = java.util.Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String privateKeyHex = java.util.Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        Map<String, String> result = new HashMap<>();
        result.put(VarEnmu.PUBLIC_KEY.value(), publicKeyHex);
        result.put(VarEnmu.PRIVATE_KEY.value(), privateKeyHex);
        return result;
    }

    /**
     * [密钥对保存]
     *
     * @param type   [type]
     * @param base64 [base64]
     * @param path   [path]
     **/
    public static void savePemToFile(String type, String base64, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(AlgEnmu.SLIGHTLY_5.algorithm())
                .append(VarEnmu.BEGIN.value())
                .append(VarEnmu.SPACE.value())
                .append(type)
                .append(AlgEnmu.SLIGHTLY_5.algorithm())
                .append(VarEnmu.LF.value());
        int index = VarEnmu.ZERO.ivalue();
        while (index < base64.length()) {
            int end = Math.min(index + VarEnmu.NUMBER_64.ivalue(), base64.length());
            sb.append(base64, index, end).append(VarEnmu.LF.value());
            index = end;
        }
        sb.append(AlgEnmu.SLIGHTLY_5.algorithm())
                .append(VarEnmu.END.value())
                .append(VarEnmu.SPACE.value())
                .append(type)
                .append(AlgEnmu.SLIGHTLY_5.algorithm())
                .append(VarEnmu.LF.value());
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * [获取公钥]
     *
     * @param pem  [pem]
     * @param type [type]
     * @return PublicKey
     **/
    public static PublicKey loadPublicKeyFromPem(String pem, String type) throws GeneralSecurityException {
        String base64 = pem
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.BEGIN.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.END.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replaceAll("\\s", VarEnmu.NONE.value());
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance(AlgEnmu.RSA.algorithm());
        return kf.generatePublic(spec);
    }

    /**
     * [获取私钥]
     *
     * @param pem  [pem]
     * @param type [type]
     * @return PrivateKey
     **/
    public static PrivateKey loadPrivateKeyFromPem(String pem, String type) throws GeneralSecurityException {
        String base64 = pem
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.BEGIN.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.END.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replaceAll("\\s", "");
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance(AlgEnmu.RSA.algorithm());
        return kf.generatePrivate(spec);
    }

    /**
     * [密钥处理]
     *
     * @param pem  [pem]
     * @param type [type]
     * @return String
     **/
    public static String keyPemToStr(String pem, String type) {
        return pem
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.BEGIN.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replace(AlgEnmu.SLIGHTLY_5.algorithm().concat(VarEnmu.END.value()).concat(VarEnmu.SPACE.value()).concat(type).concat(AlgEnmu.SLIGHTLY_5.algorithm()), VarEnmu.NONE.value())
                .replaceAll("\\s", "");
    }

}
