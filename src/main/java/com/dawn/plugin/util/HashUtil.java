package com.dawn.plugin.util;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * [哈希处理]
 * 创建时间：2021/2/1 15:49
 *
 * @author hforest-480s
 */
@Slf4j
public class HashUtil {

    /**
     * 1. md5请使用: DigestUtils.md5Hex(dat)
     * 2. sha1请使用: DigestUtils.sha1Hex(dat)
     * 3. sha256请使用: DigestUtils.sha256Hex(dat)
     * 4. sha512请使用: DigestUtils.sha512Hex(dat)
     */
    private HashUtil() {
    }

    /**
     * [hashString 签名方式]
     *
     * @param shaString
     * @param algorithm [SHA-256, SHA-512, HmacMD5, default:bigint-16]
     * @return
     */
    public static String hashString(String shaString, String algorithm) {
        String result;
        switch (algorithm) {
            case "SHA-256":
                result = DigestUtils.sha256Hex(messageDigest(shaString, algorithm));
                break;
            case "SHA-512":
                result = DigestUtils.sha512Hex(messageDigest(shaString, algorithm));
                break;
            case "HmacMD5":
                result = Base64.encodeBase64String(secretGen(shaString, algorithm, algorithm));
                break;
            default:
                result = new BigInteger(1, messageDigest(shaString, algorithm)).toString(VarEnmu.SIXTEEN.ivalue()).toLowerCase();
        }
        return result;
    }

    /**
     * [sha 签名方式]
     *
     * @param txtString
     * @param algorithm
     * @return
     */
    public static byte[] messageDigest(String txtString, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(txtString.getBytes(VarEnmu.UTF8.value()));
            return md.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            log.error(LogEnmu.LOG4.value(), "messageDigest", txtString, algorithm, ex.toString());
            return new byte[0];
        }
    }

    /**
     * [secretGen]
     *
     * @param txtString [txtString]
     * @param secret    [secret]
     * @param algorithm [HmacMD5]
     * @return
     */
    public static byte[] secretGen(String txtString, String secret, String algorithm) {
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            mac.update(txtString.getBytes(VarEnmu.UTF8.value()));
            byte[] result = mac.doFinal();
            log.debug(LogEnmu.LOG4.value(), "keyGen.result", txtString, algorithm, new BigInteger(1, result).toString(VarEnmu.SIXTEEN.ivalue()));
            return result;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException ex) {
            log.error(LogEnmu.LOG4.value(), "keyGen", txtString, algorithm, ex.toString());
            return new byte[0];
        }

    }

}
