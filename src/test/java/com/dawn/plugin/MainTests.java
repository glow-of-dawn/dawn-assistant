package com.dawn.plugin;

import cn.hutool.core.codec.Base64;
import com.vivi.plugin.enmu.LogEnmu;
import com.vivi.plugin.enmu.VarEnmu;
import com.vivi.plugin.util.CrypUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        new MainTests().file2str("file2str");
        // new MainTests().file2str("str2file");
        // new MainTests().aaa();
        log.info("-+-- over --+-");
    }

    private void file2str(String dir) throws IOException {
        var file = new File("D:/temp/test.zip");
        var tagfile = new File("D:/temp/tag.txt");
        var tagzip = new File("D:/temp/tag.zip");
        if ("file2str".equals(dir)) {
            byte[] bytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
            }
            String base64 = Base64.encode(bytes);
            log.debug("-+-- {} --+-", base64);
            FileUtils.writeStringToFile(tagfile, base64, StandardCharsets.UTF_8);
        } else if ("str2file".equals(dir)) {
            String str = FileUtils.readFileToString(tagfile, StandardCharsets.UTF_8);
            byte[] zipBytes = Base64.decode(str);
            try (FileOutputStream fos = new FileOutputStream(tagzip)) {
                fos.write(zipBytes);
            }
        }
    }

    private void aaa() throws GeneralSecurityException, IOException {
        var map = CrypUtil.generateRsaKey(VarEnmu.NUMBER_2048.ivalue());
        log.info(LogEnmu.LOG2.value(), VarEnmu.PUBLIC_KEY.value(), map.get(VarEnmu.PUBLIC_KEY.value()));
        log.info(LogEnmu.LOG2.value(), VarEnmu.PRIVATE_KEY.value(), map.get(VarEnmu.PRIVATE_KEY.value()));
        var publicKeyHex = map.get(VarEnmu.PUBLIC_KEY.value());
        var privateKeyHex = map.get(VarEnmu.PRIVATE_KEY.value());
        var publicKeyPath = "D:/temp/rust.rsa.pub";
        var privateKeyPath = "D:/temp/rust.rsa.pem";
//        CrypUtil.savePemToFile("PUBLIC KEY", publicKeyHex, publicKeyPath);
//        CrypUtil.savePemToFile("PRIVATE KEY", privateKeyHex, privateKeyPath);
        var publicPem = FileUtils.readFileToString(new File(publicKeyPath), StandardCharsets.UTF_8);
        var privatePem = FileUtils.readFileToString(new File(privateKeyPath), StandardCharsets.UTF_8);
        log.info(LogEnmu.LOG2.value(), "publicPem", publicPem);
        log.info(LogEnmu.LOG2.value(), "privatePem", privatePem);

        var publicKey = CrypUtil.loadPublicKeyFromPem(publicPem, "PUBLIC KEY");
        var privateKey = CrypUtil.loadPrivateKeyFromPem(privatePem, "PRIVATE KEY");
        log.info(LogEnmu.LOG2.value(), "publicKey", publicKey);
        log.info(LogEnmu.LOG2.value(), "privateKey", privateKey);
        publicKeyHex = CrypUtil.keyPemToStr(publicPem, "PUBLIC KEY");
        privateKeyHex = CrypUtil.keyPemToStr(privatePem, "PRIVATE KEY");
        String plaintext = "hello, this is a test message.";
        log.info(LogEnmu.LOG2.value(), "plaintext", plaintext);
        var cipher_b64 = CrypUtil.encryptBase64ByRsa(plaintext, publicKeyHex);
        log.info(LogEnmu.LOG2.value(), "cipher_b64", cipher_b64);
        var result = CrypUtil.decryptBase64ByRsa(cipher_b64, privateKeyHex);
        log.info(LogEnmu.LOG2.value(), "result", result);

        log.info(LogEnmu.LOG2.value(), "equals", result.equals(plaintext));

    }

}
