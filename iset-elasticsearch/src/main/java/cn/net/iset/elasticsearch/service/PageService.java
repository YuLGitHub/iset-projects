package cn.net.iset.elasticsearch.service;


import cn.cloudwalk.ocean.common.result.page.OceanPageAble;
import cn.cloudwalk.ocean.common.result.page.OceanPageInfo;
import cn.net.iset.elasticsearch.bean.bo.AbstractQueryBO;
import cn.net.iset.elasticsearch.bean.bo.FilterQueryBO;

import java.util.Map;

/**
 * ClassName: PageService.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @date 2020/12/24
 */
public interface PageService {
    <T extends AbstractQueryBO> OceanPageAble<Map<String, Object>> pageQuery(T queryCHBO, FilterQueryBO filterQuery, OceanPageInfo pageInfo);
}
