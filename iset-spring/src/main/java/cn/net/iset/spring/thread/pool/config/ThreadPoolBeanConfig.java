package cn.net.iset.spring.thread.pool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: ThreadPoolBeanConfig.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Configurable
@EnableAsync
public class ThreadPoolBeanConfig {

    @Autowired
    private ThreadPoolConfig threadPoolConfig;


    @Bean
    public Executor handleThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (threadPoolConfig.getCorePoolSize() != null && threadPoolConfig.getCorePoolSize() > 0) {
            executor.setCorePoolSize(threadPoolConfig.getCorePoolSize());
        }
        if (threadPoolConfig.getMaxPoolSize() != null && threadPoolConfig.getMaxPoolSize() > 0) {
            executor.setMaxPoolSize(threadPoolConfig.getMaxPoolSize());
        }
        if (threadPoolConfig.getQueueCapacity() != null && threadPoolConfig.getQueueCapacity() > 0) {
            executor.setQueueCapacity(threadPoolConfig.getQueueCapacity());
        }
        if (executor.getCorePoolSize() > executor.getMaxPoolSize()) {
            executor.setMaxPoolSize(executor.getCorePoolSize());
        }
        executor.setThreadNamePrefix("demo-event-pool");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
