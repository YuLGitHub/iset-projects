package cn.net.iset.spring.async.event.entity;

import lombok.Data;

/**
 * ClassName: DemoEvent.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Data
public class DemoEvent extends BaseEvent {

    /**
     * 若干属性
     */
    private String attributes;

    public DemoEvent(String attributes) {
        this.attributes = attributes;
    }
}
