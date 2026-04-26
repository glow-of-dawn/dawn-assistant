package com.dawn.plugin.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Padding;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [加解密工具]
 * 创建时间 2026/4/26 22:38
 *
 * @author bhyt2
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/crypt/service")
@ConditionalOnProperty(name = {"plugin-rest-controller.crypt-status"}, havingValue = "enable", matchIfMissing = true)
public class CryptRestController {

    private final PluginConfig config;

    public CryptRestController(PluginConfig config) {
        this.config = config;
    }

    @SneakyThrows
    @PostMapping("/crypt")
    public Response<Object> crypt(@RequestBody String body) {
        Map<String, String> cryptMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var algorithmType = cryptMap.getOrDefault(VarEnmu.TYPE.value(), VarEnmu.NONE.value());
        var data = cryptMap.getOrDefault(VarEnmu.DATA.value(), VarEnmu.NONE.value());
        var algorithmKey = cryptMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        var algorithmIv = cryptMap.getOrDefault(AlgEnmu.ALGORITHM_IV.algorithm(), algorithmKey);
        var privateKey = cryptMap.get(VarEnmu.PRIVATE_KEY.value());
        var publicKey = cryptMap.get(VarEnmu.PUBLIC_KEY.value());
        String value0;
        String value1 = switch (algorithmType) {
            case "sm4-encrypt" -> {
                value0 = CryptUtil.encryptBase64BySm4Cbc(algorithmKey, algorithmIv, data, Padding.PKCS5Padding, VarEnmu.UTF8.value());
                yield CryptUtil.decodeBase64BySm4Cbc(algorithmKey, algorithmIv, value0, Padding.PKCS5Padding, VarEnmu.UTF8.value());
            }
            case "sm4-decrypt" -> {
                value0 = CryptUtil.decodeBase64BySm4Cbc(algorithmKey, algorithmIv, data, Padding.PKCS5Padding, VarEnmu.UTF8.value());
                yield CryptUtil.encryptBase64BySm4Cbc(algorithmKey, algorithmIv, value0, Padding.PKCS5Padding, VarEnmu.UTF8.value());
            }
            case "aes-encrypt" -> {
                value0 = CryptUtil.encryptBase64ByWorld(algorithmKey, algorithmIv, data, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
                yield CryptUtil.decodeBase64ByWorld(algorithmKey, algorithmIv, value0, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
            }
            case "aes-decrypt" -> {
                value0 = CryptUtil.decodeBase64ByWorld(algorithmKey, algorithmIv, data, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
                yield CryptUtil.encryptBase64ByWorld(algorithmKey, algorithmIv, value0, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
            }
            case "sm2-encrypt" -> {
                value0 = CryptUtil.encryptBase64BySm2(data, publicKey);
                yield CryptUtil.decodeBase64BySm2(value0, privateKey);
            }
            case "sm2-decrypt" -> {
                value0 = CryptUtil.decodeBase64BySm2(data, privateKey);
                yield CryptUtil.encryptBase64BySm2(value0, publicKey);
            }
            case "rsa-encrypt" -> {
                value0 = CryptUtil.encryptBase64ByRsa(data, publicKey);
                yield CryptUtil.decryptBase64ByRsa(value0, privateKey);
            }
            case "rsa-decrypt" -> {
                value0 = CryptUtil.decryptBase64ByRsa(data, privateKey);
                yield CryptUtil.encryptBase64ByRsa(value0, publicKey);
            }
            default -> {
                value0 = data;
                yield data;
            }
        };
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ZERO.value()), value0);
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ONE.value()), value1);
        if (algorithmType.contains("SM2")) {
            cryptMap.put(VarEnmu.MESSAGE.value(), value1.equals(VarEnmu.NONE.value()) ? "结果不可用" : "结果可用");
        } else {
            cryptMap.put(VarEnmu.MESSAGE.value(), value1.equals(data) ? "结果可用" : "结果不可用");
        }
        cryptMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue()));
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.ONE.value()), "sm4-encryp");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.TWO.value()), "sm4-decryp");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.THREE.value()), "aes-encryp");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FOUR.value()), "aes-decryp");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FIVE.value()), "sm2-encryp");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SIX.value()), "sm2-decryp");
        return value1.equals(VarEnmu.NONE.value())
            ? new Response<>().data(cryptMap).success().message("结果无输出")
            : new Response<>().data(cryptMap).success().message("结果已输出");
    }

    @SneakyThrows
    @PostMapping("/crypt2")
    public Response<Object> crypt2(@RequestBody String body) {
        Map<String, String> cryptMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var algorithmType = cryptMap.getOrDefault(VarEnmu.TYPE.value(), VarEnmu.NONE.value());
        var data = cryptMap.getOrDefault(VarEnmu.DATA.value(), VarEnmu.NONE.value());
        String value0;
        String value1;
        switch (algorithmType) {
            case "base64-encode":
                value0 = Base64.encode(data);
                value1 = Base64.decodeStr(value0);
                break;
            case "base64-decode":
                value0 = Base64.decodeStr(data);
                value1 = Base64.encode(value0);
                break;
            default:
                value0 = data;
                value1 = data;
        }
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ZERO.value()), value0);
        cryptMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ONE.value()), value1);
        cryptMap.put(VarEnmu.MESSAGE.value(), value1.equals(data) ? "结果可用" : "结果不可用2");

        cryptMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue()));
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SEVEN.value()), "base64-encode");
        cryptMap.put(VarEnmu.TYPE.value().concat(VarEnmu.EIGHT.value()), "base64-decode");
        return value1.equals(data)
            ? new Response<>().data(cryptMap).success().message("结果可用")
            : new Response<>().data(cryptMap).success().message("结果不可用2");
    }

}
