package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.CryptRestService;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [加解密工具]
 * 创建时间 2026/4/26 22:38
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/crypt")
@ConditionalOnProperty(name = {"plugin-rest-controller.crypt-status"}, havingValue = "enable", matchIfMissing = true)
public class CryptRestController {

    private final CryptRestService cryptRestService;

    public CryptRestController(CryptRestService cryptRestService) {
        this.cryptRestService = cryptRestService;
    }

    @SneakyThrows
    @PostMapping("/group-a")
    public Response<Object> groupByA(@RequestBody String body) {
        return cryptRestService.groupByA(body);
    }

    @SneakyThrows
    @PostMapping("/group-b")
    public Response<Object> groupByB(@RequestBody String body) {
        return cryptRestService.groupByB(body);
    }

    @SneakyThrows
    @GetMapping("/generate/{keyType}/{keySize}")
    public Response<Object> generateKey(@PathVariable("keyType") String keyType, @PathVariable("keySize") int keySize) {
        return cryptRestService.generateKey(keyType, keySize);
    }

}
