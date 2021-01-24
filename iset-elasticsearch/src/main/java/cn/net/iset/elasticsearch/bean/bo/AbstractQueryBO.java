package cn.net.iset.elasticsearch.bean.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * ClassName: AbstractQueryBO.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @since 2020/12/24
 */
@Data
public class AbstractQueryBO implements Serializable {

    /**
     * 排序ID
     */
    private Long orderId;


    /**
     * 抓拍开始时间
     **/
    protected Long captureTimeB;

    /**
     * 抓拍结束时间
     **/
    protected Long captureTimeE;


    /**
     * 需要映射的目标字段
     */
    protected Set<String> targetFields;

}
