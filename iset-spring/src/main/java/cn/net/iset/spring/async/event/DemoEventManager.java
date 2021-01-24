package cn.net.iset.spring.async.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ClassName: DemoEventManager.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Slf4j
@Component
public class DemoEventManager extends BaseEventManager {

    /**
     * 无参构造设置事件管理器的名称
     */
    public DemoEventManager() {
        this.managerName = "DemoEventManager";
    }

    /**
     * 初始化线程池
     */
    @PostConstruct
    public void init() {
        super.initEventManager();
    }
}
