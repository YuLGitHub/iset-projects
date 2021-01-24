package cn.net.iset.elasticsearch.bean;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @ClassName:
 * @Author:jiangjipeng
 * @Date:Created in 3:19 下午 2020/11/18
 * @Description:索引信息
 * @Version:
 * @Email:jiangjipeng@cloudwalk.cn
 */
@Data
@Component
public class IndexDO {
    /**
     * 索引名字 格式：
     * 人脸-人体-车：capture-allstruct-fbv-15-1-1-20201118
     * 人脸-人体-非车: capture-allstruct-fbnv-15-1-1-20201118
     */
    private String indexName;

    /**
     * 当前索引所在桶编号
     */
//    private Integer indexBucketId;

    /**
     * 索引的排序id
     */
    private Long orderId;

    /**
     * 索引类型
     *  1.人脸+人体+机动车
     *  2.人脸+人体+非机动车
     */
    private Integer indexType;
}
