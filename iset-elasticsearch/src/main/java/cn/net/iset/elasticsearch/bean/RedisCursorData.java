package cn.net.iset.elasticsearch.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: RedisCursorData.java
 * Description:
 *
 * @author yule
 * @date 2020/11/22
 */
@Data
public class RedisCursorData implements Serializable {
    private static final long serialVersionUID = 8588642339298872693L;

    /**
     * 开始标记 -- 时间
     */
    private Long startMarkTime;
    /**
     * 结束标记 -- 时间
     */
    private Long endMarkTime;
    /**
     * 开始标记 -- id
     */
    private Long startMarkId;
    /**
     * 结束标记 -- id
     */
    private Long endMarkId;

    /**
     * 当前索引所在桶编号
     */
    private Integer indexBucketId;

    /**
     * 当前页码
     */
    private Integer currentPage;
}
