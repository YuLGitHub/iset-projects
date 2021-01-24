package cn.net.iset.spring.async.event;

import cn.net.iset.spring.async.event.config.InnerEventConfig;
import cn.net.iset.spring.async.event.entity.BaseEvent;
import cn.net.iset.spring.async.event.handle.EventHandler;
import cn.net.iset.spring.async.event.utils.ThreadUtil;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: BaseEventManager.java
 * Description:
 *
 * @author yule
 * @date 2021/01/26
 */
@Slf4j
public abstract class BaseEventManager implements DisposableBean {

    /**
     * 内部事件通知配置类
     */
    @Autowired
    protected InnerEventConfig innerEventConfig;

    /**
     * 自定义线程池
     */
    protected ThreadPoolExecutor threadPool;

    protected EventBus eventBus;

    protected String managerName;

    public String getManagerName() {
        return managerName;
    }

    protected void initEventManager() {
        threadPool = new ThreadPoolExecutor(innerEventConfig.getCorePoolSize(), innerEventConfig.getMaxPoolSize()
                , 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(innerEventConfig.getQueueSize())
                , ThreadUtil.renameThread("[" + getManagerName() + "]--[eventBusPool]--%d", false)
                , new ThreadPoolExecutor.CallerRunsPolicy());
        eventBus = new AsyncEventBus(threadPool);
    }


    /**
     * 添加server相关事件的监听
     *
     * @param handler
     */
    public void addEventListener(EventHandler handler) {
        eventBus.register(handler);
        log.info("[" + getManagerName() + "]--[addEventListener]--[add eventHandler{}]", handler.getClass().getName());
    }

    /**
     * 取消监听
     *
     * @param handler
     */
    public void deleteEventListener(EventHandler handler) {
        eventBus.unregister(handler);
        log.info("[" + getManagerName() + "]--[deleteEventListener]--[delete eventHandler{}]", handler.getClass().getName());
    }


    /**
     * 发布事件
     *
     * @param event
     * @param <T>
     */
    public <T extends BaseEvent> void postEvent(T event) {
        eventBus.post(event);
        log.debug("[" + getManagerName() + "]--[postEvent]--[post event{}]", event.getClass().getName());
    }

    @Override
    public void destroy() throws Exception {
        if (null != this.threadPool) {
            this.threadPool.shutdownNow();
        }
    }
}
