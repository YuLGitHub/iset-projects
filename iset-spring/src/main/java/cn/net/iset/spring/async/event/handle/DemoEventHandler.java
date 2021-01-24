package cn.net.iset.spring.async.event.handle;

import cn.net.iset.spring.async.event.DemoEventManager;
import cn.net.iset.spring.async.event.entity.DemoEvent;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ClassName: DemoEventHandler.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Slf4j
@Component
public class DemoEventHandler implements EventHandler<DemoEvent>{

    /**
     * 事件管理器
     */
    @Autowired
    private DemoEventManager eventManager;

    /**
     * 初始化添加监听器
     */
    @PostConstruct
    public void init() {
        eventManager.addEventListener(this);
    }

    /**
     * 时间处理方法
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    @Override
    public void handle(DemoEvent event) {

        // todo something
        final String attributes = event.getAttributes();

        log.info("do something with {}", attributes);
    }
}
