package cn.net.iset.spring.async.event.handle;

/**
 * ClassName: InnerEventConfig.java
 * Description: 事件处理类
 *
 * @author yule
 * @since 2021/1/24
 */
@FunctionalInterface
public interface EventHandler<T> {


    /**
     * 具体时间处理
     * @param event
     */
    void handle(final T event);
}
