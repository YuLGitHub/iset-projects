package cn.net.iset.spring.thread.pool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: ThreadPoolConfig.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Data
@Component
@ConfigurationProperties("thread.pool.config")
public class ThreadPoolConfig {
    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize;

    /**
     * 线程队列数
     */
    private Integer queueCapacity;

    /**
     * 无参构造设置线程池默认线程参数
     */
    public ThreadPoolConfig() {
        this.corePoolSize = 10;
        this.maxPoolSize = 200;
        this.queueCapacity = 30;
    }
}
