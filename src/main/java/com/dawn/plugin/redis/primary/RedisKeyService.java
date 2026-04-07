package com.dawn.plugin.redis.primary;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.redis.primary.impl.DatabaseKeyServiceImpl;
import com.dawn.plugin.redis.primary.impl.RedisKeyServiceImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 依赖于redis的键值获取
 * 创建时间：2021/2/2 23:33
 *
 * @author hforest-480s
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisKeyService extends AbstractRedisKeyService implements KeyService {

    private RedisKeyServiceImpl redisKeyServiceImpl;
    private DatabaseKeyServiceImpl databaseKeyServiceImpl;
    private KeyService keyService;
    private boolean redisHealth = true;

    public RedisKeyService(RedisTemplate<String, Object> redisTemplate,
                           RedisKeyServiceImpl redisKeyServiceImpl,
                           DatabaseKeyServiceImpl databaseKeyServiceImpl) {
        this.redisTemplate = redisTemplate;
        this.redisKeyServiceImpl = redisKeyServiceImpl;
        this.databaseKeyServiceImpl = databaseKeyServiceImpl;
        /* 默认redis模式 */
        this.keyService = redisKeyServiceImpl;
    }

    /**
     * 键制调整
     */
    public void flushRedisKeyService() {
        if (redisHealth) {
            this.keyService = redisKeyServiceImpl;
        } else {
            this.keyService = databaseKeyServiceImpl;
        }
    }

    @Override
    public String getPrimary() {
        return keyService.getPrimary();
    }

    @Override
    public String getPrimary(final int digLen) {
        return keyService.getPrimary(digLen);
    }

    @Override
    public String getTimestamp() {
        return keyService.getTimestamp();
    }

    @Override
    public String roundNo() {
        return keyService.roundNo();
    }

    @Override
    public String roundNo(final String lastKey) {
        return keyService.roundNo(lastKey);
    }

    /**
     * 循环值，定时回归 0
     *
     * @param lastKey [key 末尾串 标识: redis.key: x-x-lastKey]
     * @param digLen  [digLen]
     * @return String 返回串，定长补 0
     */
    @Override
    public String roundNo(String lastKey, int digLen) {
        return keyService.roundNo(lastKey, digLen);
    }

    /**
     * 获取一个16位随机串 可用做AES、SM4口令 / 建议预设在数据库中
     *
     * @param lastKey [key 末尾串 标识: redis.key: x-x-lastKey]
     * @return String 返回串，定长补 0
     */
    @Override
    public String getKeyLen16(String lastKey) {
        return keyService.getKeyLen16(lastKey);
    }

    /**
     * 获取 algorithm-key
     *
     * @param authToken [authToken]
     * @return String 返回串，定长补 0
     */
    @Override
    public String getAlgorithmKey(String authToken) {
        return keyService.getAlgorithmKey(authToken);
    }

    @Override
    public boolean isRedisHealth() {
        return redisHealth;
    }

    /**
     * Redis 健康检查
     *
     */
    public void redisHealth() {
        try {
            String pong = Optional.ofNullable(redisTemplate.getConnectionFactory())
                    .orElseThrow()
                    .getConnection()
                    .ping();
            redisHealth = "PONG".equals(pong);
        } catch (Exception e) {
            redisHealth = false;
            log.warn(LogEnmu.LOG2.value(), "redis服务链接失效", e.toString());
        }
    }

}
