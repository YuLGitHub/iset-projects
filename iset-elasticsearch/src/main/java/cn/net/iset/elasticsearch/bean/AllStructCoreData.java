package cn.net.iset.elasticsearch.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: AllStructCoreData.java
 * Description:
 *
 * @author yule
 * @date 2020/11/22
 */
@Data
public class AllStructCoreData implements Serializable {
    private static final long serialVersionUID = 1115878826978979883L;

    /**
     * 数据ID
     */
    private Long dataId;

    /**
     * 抓拍时间
     */
    private Long captureTime;

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 当前索引所在桶编号
     */
    private Integer indexBucketId;
}
