package cn.net.iset.spring.async.event.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

/**
 * ClassName: ThreadUtil.java
 * Description: 基础事件
 *
 * @author yule
 * @since 2021/1/24
 */
public class ThreadUtil {

    /**
     * 获取线程工厂
     * @param name
     * @param daemon
     * @return
     */
    public static ThreadFactory renameThread(String name, boolean daemon) {
        return new ThreadFactoryBuilder()
                .setNameFormat(name).setDaemon(daemon)
                .build();
    }
}
