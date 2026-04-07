package com.dawn.plugin.redis.primary;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.mapper.ccore.TabParamsMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 创建时间：2025/7/3 15:22
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public abstract class AbstractRedisKeyService {

    @Value("#{'${spring.application.name}:authtoken:'}")
    protected String redisAuthtokenKey;
    /* redis前缀 */
    @Value("#{'${spring.application.name}:'}")
    protected String redisHeader;
    /* redis前缀 */
    @Value("#{'${spring.application.name}:round:'}")
    protected String redisRoundHeader;
    /* redis前缀 */
    @Value("#{'${spring.application.name}:aes:'}")
    protected String redisAesHeader;
    /* RoundNo 位数上限 4 = [0000 ~ 9999] */
    @Value("${spring.data.redis.round-digLen:4}")
    protected int digLen = VarEnmu.FOUR.ivalue();
    /* redis 保留时常 */
    @Value("${spring.data.redis.redis-expires:3600}")
    protected int redisExpires = 3600;
    /* redis 保留时常 - shot */
    protected int redisShot1sExpires = 1;
    protected int redisShot2sExpires = 2;
    protected int redisShot3sExpires = 3;
    protected int redisShot5sExpires = 5;
    protected int redisShot10sExpires = 10;
    protected int redisShot20sExpires = 20;
    protected int redisShot30sExpires = 30;
    protected int redisShot1mExpires = 60;
    protected int redisShot2mExpires = 120;
    protected int redisShot3mExpires = 180;
    protected int redisShot5mExpires = 300;
    protected int redisShot10mExpires = 600;
    protected int redisShot20mExpires = 1200;
    protected int redisShot30mExpires = 1800;
    protected int redisShot1hExpires = 3600;
    protected int redisShot2hExpires = 7200;
    protected int redisShot3hExpires = 10800;
    protected int redisShot6hExpires = 21600;
    protected int redisShot12hExpires = 43200;
    protected int redisShot24hExpires = 86400;
    @Value("${spring.data.redis.redis-shot-expires:60}")
    protected int redisShotExpires = 60;
    protected RedisTemplate<String, Object> redisTemplate;
    protected TabParamsMapper tabParamsMapper;
    protected PluginConfig config;

    /**
     * 主键获取
     *
     * @return String 主键
     */
    public String getPrimary() {
        String timestamp = String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
        String rno = roundNo();
        return String.join(VarEnmu.NONE.value(), timestamp, StringUtils.right(rno, digLen));
    }

    /**
     * 主键获取
     *
     * @return String 主键
     */
    public String getPrimary(int digLen) {
        String timestamp = String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
        String rno = roundNo(VarEnmu.INCREMENT_RROUND_NO.value(), digLen);
        return String.join(VarEnmu.NONE.value(), timestamp, StringUtils.right(rno, digLen));
    }

    /**
     * 时间戳获取
     *
     * @return String 主键
     */
    public String getTimestamp() {
        return String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
    }

    /**
     * 循环值，定时回归 0
     *
     * @return String 返回串，定长补 0
     */
    public String roundNo() {
        return roundNo(VarEnmu.INCREMENT_RROUND_NO.value(), digLen);
    }

    /**
     * 循环值，定时回归 0
     *
     * @return String 返回串，定长补 0
     */
    public String roundNo(String lastKey) {
        return roundNo(lastKey, digLen);
    }

    /**
     * 循环值，定时回归 0
     *
     * @param lastKey [key 末尾串 标识: redis.key: xxxx-xxxx-lastKey]
     * @param digLen  [digLen]
     * @return String 返回串，定长补 0
     */
    public String roundNo(final String lastKey, final int digLen) {
        log.debug(LogEnmu.LOG4.value(), "[roundNo]函数待实现");
        return "[roundNo]函数待实现";
    }

    /**
     * 获取一个16位随机串 可用做AES、SM4口令
     *
     * @param lastKey [key 末尾串 标识: redis.key: xxxx-xxxx-lastKey]
     * @return String 返回串，定长补 0
     */
    public String getKeyLen16(final String lastKey) {
        log.debug(LogEnmu.LOG4.value(), "[getKeyLen16]函数待实现");
        return "[getKeyLen16]函数待实现";
    }

    /**
     * 获取 algorithm-key
     *
     * @param authToken [authToken]
     * @return String 返回串，定长补 0
     */
    public String getAlgorithmKey(final String authToken) {
        log.debug(LogEnmu.LOG4.value(), "[getAlgorithmKey]函数待实现");
        return "[getAlgorithmKey]函数待实现";
    }

    /**
     * Redis 健康检查
     *
     * @return boolean
     */
    public boolean isRedisHealth() {
        log.debug(LogEnmu.LOG4.value(), "[getRedisHealth]函数待实现");
        return false;
    }

}
