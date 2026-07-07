package com.dawn.plugin.system;

import org.springframework.stereotype.Component;

/**
 * [需要执行的关闭]
 * 创建时间：2021/4/9 11:10
 *
 * @author hforest-480s
 */
@Component
public interface ShutdownService {

    /**
     * [程序处理]
     *
     **/
    void shutdown();

}
