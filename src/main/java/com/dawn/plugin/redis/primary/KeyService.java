package com.dawn.plugin.redis.primary;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 创建时间：2025/7/3 15:30
 *
 * @author hforest-480s
 */
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public interface KeyService {

    /**
     * 主键获取
     *
     * @return String 主键
     */
    String getPrimary();

    /**
     * 主键获取
     *
     * @return String 主键
     */
    String getPrimary(int digLen);

    /**
     * 时间戳获取
     *
     * @return String 主键
     */
    String getTimestamp();

    /**
     * 循环值，定时回归 0
     *
     * @return String 返回串，定长补 0
     */
    String roundNo();

    /**
     * 循环值，定时回归 0
     *
     * @return String 返回串，定长补 0
     */
    String roundNo(String lastKey);

    /**
     * 循环值，定时回归 0
     *
     * @param lastKey [key 末尾串 标识: redis.key: xxxx-xxxx-lastKey]
     * @param digLen  [digLen]
     * @return String 返回串，定长补 0
     */
    String roundNo(String lastKey, int digLen);

    /**
     * 获取一个16位随机串 可用做AES、SM4口令 / 建议预设在数据库中
     *
     * @param lastKey [key 末尾串 标识: redis.key: xxxx-xxxx-lastKey]
     * @return String 返回串，定长补 0
     */
    String getKeyLen16(String lastKey);

    /**
     * 获取 algorithm-key
     *
     * @param authToken
     * @return String 返回串，定长补 0
     */
    String getAlgorithmKey(String authToken);

    boolean isRedisHealth();

}
