package cn.net.iset.spring.thread.pool.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * ClassName: ThreadPoolDemo.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Slf4j
@Component
public class ThreadPoolDemo {

    @Resource(name = "handleThreadPoolExecutor")
    private Executor executor;


    public void doSomething() {
        Map<String, Object> params = new HashMap<>(16);
        // 将任务放入线程池执行
        executor.execute(() -> runSomething(params));
    }

    private void runSomething(Map<String, Object> params) {
        // run something
        log.info("run something with {}", params);
    }
}
