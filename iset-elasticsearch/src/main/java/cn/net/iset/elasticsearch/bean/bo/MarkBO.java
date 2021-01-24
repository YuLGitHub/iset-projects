package cn.net.iset.elasticsearch.bean.bo;

import lombok.Data;

/**
 * @ClassName:
 * @Author:jiangjipeng
 * @Date:Created in 11:02 上午 2020/11/24
 * @Description:
 * @Version:
 * @Email:jiangjipeng@cloudwalk.cn
 */
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
