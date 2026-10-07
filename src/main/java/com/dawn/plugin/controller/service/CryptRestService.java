package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 加解密工具
 * 创建时间 2026/8/26 20:38
 *
 * @author bhyt2
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-rest-controller.crypt-status"}, havingValue = "enable", matchIfMissing = true)
public class CryptRestService {

    private final PluginConfig config;

    public CryptRestService(PluginConfig config) {
        this.config = config;
    }

    public Response<Object> crypt(String body) {
        Map<String, String> cryptMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var algorithmType = cryptMap.getOrDefault(VarEnmu.TYPE.value(), VarEnmu.NONE.value());
        var data = cryptMap.getOrDefault(VarEnmu.DATA.value(), VarEnmu.NONE.value());
        var algorithmKey = cryptMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        var algorithmIv = cryptMap.getOrDefault(AlgEnmu.ALGORITHM_IV.algorithm(), algorithmKey);
        var privateKey = cryptMap.get(VarEnmu.PRIVATE_KEY.value());
        var publicKey = cryptMap.get(VarEnmu.PUBLIC_KEY.value());
        var result = CryptUtil.crypt(algorithmType, data, algorithmKey, algorithmIv, publicKey, privateKey);
        String value0 = result[0];
        String value1 = result[1];
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ZERO.value()), value0);
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ONE.value()), value1);
        if (algorithmType.contains("SM2")) {
            cryptMap.put(VarEnmu.MESSAGE.value(), value1.equals(VarEnmu.NONE.value()) ? "结果不可用" : "结果可用");
        } else {
            cryptMap.put(VarEnmu.MESSAGE.value(), value1.equals(data) ? "结果可用" : "结果不可用");
        }
        cryptMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue()));
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.ONE.value()), "sm4-encrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.TWO.value()), "sm4-decrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.THREE.value()), "aes-encrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FOUR.value()), "aes-decrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FIVE.value()), "sm2-encrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SIX.value()), "sm2-decrypt");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SEVEN.value()), "base64-encode");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.EIGHT.value()), "base64-decode");
        return new Response<>().data(cryptMap).success().message(value1.equals(data) ? "结果无输出" : "结果已输出");
    }

    public Response<Object> generateKey(String keyType, int keySize) throws NoSuchAlgorithmException {
        Map<String, String> keyMap = switch (keyType) {
            case "RSA" -> CryptUtil.generateRsaKey(keySize);
            case "SM2" -> CryptUtil.generateSm2Key();
            default -> CryptUtil.generateRsaKey(keySize);
        };
        return new Response<>().data(keyMap).success().message("RSA/SM2");
    }

}
