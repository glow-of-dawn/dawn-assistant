package com.dawn.plugin.controller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * [消息队列]
 * 创建时间 2026/8/26 20:38
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.svr-status"}, havingValue = "enable", matchIfMissing = true)
public class QueueRedisRestService {
}
