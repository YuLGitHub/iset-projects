package cn.net.iset.spring.async.event.demo;

import cn.net.iset.spring.async.event.DemoEventManager;
import cn.net.iset.spring.async.event.entity.DemoEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: EventDemo.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Component
public class EventDemo {

    @Autowired
    private DemoEventManager demoEventManager;

    /**
     * 发送事件
     */
    public void doSomething() {
        demoEventManager.postEvent(new DemoEvent("doSomething"));
    }
}
