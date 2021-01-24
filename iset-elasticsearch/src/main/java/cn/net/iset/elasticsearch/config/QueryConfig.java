package cn.net.iset.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: QueryConfig.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @date 2020/11/18
 */
@Data
@Component
@ConfigurationProperties(prefix = "query.config")
public class QueryConfig {

    /**
     * 查询redis过期时间
     */
    private Integer redisExpiredTime;

    /**
     * 索引归并查询个数
     */
    private Integer fragmentIndexCount;

    /**
     * 设备归并查询个数
     */
    private Integer fragmentDeviceCount;

    /**
     * 线程池核心数
     */
    private Integer corePoolSize;

    /**
     * 线程池最大
     */
    private Integer maxPoolSize;

    /**
     * 滚动数量
     */
    private Integer rollSize;

    /**
     * 所有分页查询ES查询标记
     */
    private Integer elasticsearchPage;

    public QueryConfig() {
        this.redisExpiredTime = 180;
        this.fragmentIndexCount = 4;
        this.fragmentDeviceCount = 2000;
        this.corePoolSize = 50;
        this.maxPoolSize = 500;
        this.rollSize = 5000;
        this.elasticsearchPage = 0;
    }
}
