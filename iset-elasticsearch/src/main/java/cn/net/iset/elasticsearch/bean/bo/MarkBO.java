package cn.net.iset.elasticsearch.bean.bo;

import lombok.Data;

@Data
public class MarkBO {
    /**
     * 文档id
     */
    private String id;

    /**
     * 标记时间
     */
    private Long markTime;

    /**
     * orderId
     */
    private Long orderId;

}
