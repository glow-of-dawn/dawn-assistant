package com.dawn.plugin;

import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * @author hforest-480s
 * @date 2021/2/4 12:02
 */
@Slf4j
public class MainTests {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, GeneralSecurityException {
        log.info("-+-- start --+-");
        // new MainTests().aaa();
        log.info("-+-- over --+-");
    }

    private void bbb() {
        var algorithmKey = "vAr8K5fZxrT4iTo5";
        var dat01 = "hello, this is a test message.";
        var dat02 = "CblqYlHSQKd87LTVudA3RA==";
        var algorithmIv = "vAr8K5fZxrT4iTo5";
        log.info(LogEnmu.LOG2.value(), "encrypted data", dat01);
        log.info(LogEnmu.LOG2.value(), "decrypted data", dat02);
        var val = CryptUtil.encryptBase64ByWorld(algorithmKey, algorithmIv, dat01, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
        log.info(LogEnmu.LOG2.value(), "encrypt data", val);
        var dat = CryptUtil.decodeBase64ByWorld(algorithmKey, algorithmIv, dat02, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
        log.info(LogEnmu.LOG2.value(), "decrypt data", dat);
    }

    private void aaa() throws GeneralSecurityException, IOException {
        var map = CryptUtil.generateRsaKey(VarEnmu.NUMBER_2048.ivalue());
        log.info(LogEnmu.LOG2.value(), VarEnmu.PUBLIC_KEY.value(), map.get(VarEnmu.PUBLIC_KEY.value()));
        log.info(LogEnmu.LOG2.value(), VarEnmu.PRIVATE_KEY.value(), map.get(VarEnmu.PRIVATE_KEY.value()));
        var publicKeyHex = map.get(VarEnmu.PUBLIC_KEY.value());
        var privateKeyHex = map.get(VarEnmu.PRIVATE_KEY.value());
        var publicKeyPath = "D:/temp/rust.rsa.pub";
        var privateKeyPath = "D:/temp/rust.rsa.pem";
//        CryptUtil.savePemToFile("PUBLIC KEY", publicKeyHex, publicKeyPath);
//        CryptUtil.savePemToFile("PRIVATE KEY", privateKeyHex, privateKeyPath);
        var publicPem = FileUtils.readFileToString(new File(publicKeyPath), StandardCharsets.UTF_8);
        var privatePem = FileUtils.readFileToString(new File(privateKeyPath), StandardCharsets.UTF_8);
        log.info(LogEnmu.LOG2.value(), "publicPem", publicPem);
        log.info(LogEnmu.LOG2.value(), "privatePem", privatePem);

        var publicKey = CryptUtil.loadPublicKeyFromPem(publicPem, "PUBLIC KEY");
        var privateKey = CryptUtil.loadPrivateKeyFromPem(privatePem, "PRIVATE KEY");
        log.info(LogEnmu.LOG2.value(), "publicKey", publicKey);
        log.info(LogEnmu.LOG2.value(), "privateKey", privateKey);
        publicKeyHex = CryptUtil.keyPemToStr(publicPem, "PUBLIC KEY");
        privateKeyHex = CryptUtil.keyPemToStr(privatePem, "PRIVATE KEY");
        String plaintext = "hello, this is a test message.";
        log.info(LogEnmu.LOG2.value(), "plaintext", plaintext);
        var cipher_b64 = CryptUtil.encryptBase64ByRsa(plaintext, publicKeyHex);
        log.info(LogEnmu.LOG2.value(), "cipher_b64", cipher_b64);
        var result = CryptUtil.decryptBase64ByRsa(cipher_b64, privateKeyHex);
        log.info(LogEnmu.LOG2.value(), "result", result);

        log.info(LogEnmu.LOG2.value(), "equals", result.equals(plaintext));

    }

}
