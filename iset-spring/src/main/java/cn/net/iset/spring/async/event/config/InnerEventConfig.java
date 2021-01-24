package cn.net.iset.spring.async.event.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: InnerEventConfig.java
 * Description: 内部事件通知配置
 *
 * @author yule
 * @since 2021/1/24
 */
@Data
@Component
@ConfigurationProperties(prefix = "inner.event")
public class InnerEventConfig {

    /**
     * 最小线程数量
     */
    private Integer corePoolSize = Runtime.getRuntime().availableProcessors() * 8;

    private Integer maxPoolSize = Runtime.getRuntime().availableProcessors() * 80;

    private Integer queueSize = Runtime.getRuntime().availableProcessors();

}
