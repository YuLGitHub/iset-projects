package cn.net.iset.elasticsearch.bean;

import lombok.Data;

import java.util.List;

/**
 * ClassName: EachDetailDataResult.java
 * Description:
 *
 * @author yule
 * @date 2020/11/26
 */
@Data
public class EachDetailDataResult {

    /**
     * 全部数据
     */
    private List<AllStructCoreData> allStructCoreData;

    /**
     * 命中的那一页
     */
    private List<AllStructCoreData> hitPageCoreData;

    /**
     * 是否从后取值
     */
    private Boolean fromLastPage;


    /**
     * 开始页码
     */
    private Integer startPage;

    /**
     * 结束页码
     */
    private Integer endPage;
}
